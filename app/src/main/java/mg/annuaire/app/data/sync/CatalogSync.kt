package mg.annuaire.app.data.sync

import android.content.Context
import android.util.Log
import java.io.File
import mg.annuaire.app.data.local.AnnuaireDao
import mg.annuaire.app.data.model.Avis
import mg.annuaire.app.data.model.Commune
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.MetierStatus
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.data.model.PrestataireQuartier
import mg.annuaire.app.data.model.Quartier
import mg.annuaire.app.data.model.Tarif
import mg.annuaire.app.data.model.User
import mg.annuaire.app.data.remote.AnnuaireApi
import mg.annuaire.app.data.remote.CatalogDto
import mg.annuaire.app.data.remote.CertificationPatchDto
import mg.annuaire.app.data.remote.DossierDto
import mg.annuaire.app.data.remote.MetierProposeDto
import mg.annuaire.app.data.remote.NetworkModule
import kotlinx.coroutines.flow.first
import mg.annuaire.app.data.session.SettingsStore

/**
 * Offline-first : Room = source de vérité.
 * Sync réseau selon le réglage (Wi‑Fi par défaut).
 */
class CatalogSync(
    private val context: Context,
    private val dao: AnnuaireDao,
    private val api: AnnuaireApi,
    private val settings: SettingsStore
) {
    sealed class Result {
        data class Ok(val source: String, val communes: Int, val quartiers: Int) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun sync(): Result {
        val catalog = resolveCatalog()
        applyCatalog(catalog.first)
        mergeRemotePhotos()
        mergeRemoteDecisions()
        return Result.Ok(
            source = catalog.second,
            communes = catalog.first.communes.size,
            quartiers = catalog.first.quartiers.size
        )
    }

    private suspend fun resolveCatalog(): Pair<CatalogDto, String> {
        val mode = settings.networkMode.first()
        if (!WifiChecker.allows(context, mode)) {
            Log.i(TAG, "Réseau non autorisé → fallback assets")
            return loadAssets(WifiChecker.fallbackLabel(mode))
        }
        return try {
            val remote = loadRemoteCatalog()
                ?: return loadAssets("réseau · catalogue local")
            Log.i(TAG, "Sync OK (version=${remote.version})")
            remote to WifiChecker.successLabel(mode)
        } catch (e: Exception) {
            Log.w(TAG, "Sync échouée, fallback assets: ${e.message}")
            loadAssets("réseau · catalogue local")
        }
    }

    private suspend fun networkAllowed(): Boolean =
        WifiChecker.allows(context, settings.networkMode.first())

    private suspend fun loadRemoteCatalog(): CatalogDto? {
        val nested = runCatching { api.getCatalog() }.getOrNull()
        if (nested != null && hasCatalogData(nested)) return nested
        val root = runCatching { api.getRootCatalog() }.getOrNull()
        if (root != null && hasCatalogData(root)) return root
        return null
    }

    private fun hasCatalogData(catalog: CatalogDto): Boolean =
        catalog.prestataires.isNotEmpty() || catalog.communes.isNotEmpty()

    private fun loadAssets(label: String): Pair<CatalogDto, String> {
        return NetworkModule.loadCatalogFromAssets(context) to label
    }

    private suspend fun applyCatalog(catalog: CatalogDto) {
        dao.replaceReferenceData(
            metiers = catalog.metiers.map {
                Metier(
                    id = it.id,
                    nom = it.nom,
                    status = it.status.ifBlank { MetierStatus.APPROVED.name }
                )
            },
            communes = catalog.communes.map {
                Commune(it.id, it.nom, it.district, it.type)
            },
            quartiers = catalog.quartiers.map {
                Quartier(it.id, it.communeId, it.nom)
            }
        )

        catalog.usersDemo.forEach { u ->
            if (dao.findUserByPhone(u.telephone) == null) {
                dao.insertUser(
                    User(
                        nom = u.nom,
                        telephone = u.telephone,
                        password = u.password,
                        role = u.role,
                        communeId = u.communeId
                    )
                )
            }
        }

        catalog.prestataires.forEach { p ->
            val linkedUserId = if (p.linkedDemoUser) {
                dao.findUserByPhone(p.telephone)?.id
            } else null

            val existing = dao.findPrestataireById(p.id)
            if (existing == null) {
                dao.upsertPrestataire(
                    Prestataire(
                        id = p.id,
                        userId = linkedUserId,
                        nom = p.nom,
                        telephone = p.telephone,
                        metierId = p.metierId,
                        communeId = p.communeId,
                        description = p.description,
                        disponibleAujourdhui = p.disponibleAujourdhui,
                        certificationStatus = p.certificationStatus,
                        cinNumero = p.cinNumero ?: p.patente,
                        commentaireAgent = p.commentaireAgent
                    )
                )
                dao.clearQuartiers(p.id)
                dao.insertPrestataireQuartiers(
                    p.quartierIds.map { PrestataireQuartier(p.id, it) }
                )
                dao.clearTarifs(p.id)
                dao.insertTarifs(
                    p.tarifs.map { Tarif(prestataireId = p.id, libelle = it.libelle, montantAr = it.montantAr) }
                )
            } else {
                var row = existing
                if (row.userId == null && linkedUserId != null) {
                    row = row.copy(userId = linkedUserId)
                    dao.updatePrestataire(row)
                }
                if (p.certificationStatus == "CERTIFIED" || p.certificationStatus == "REJECTED") {
                    dao.updatePrestataire(
                        row.copy(
                            certificationStatus = p.certificationStatus,
                            cinNumero = p.cinNumero ?: p.patente ?: row.cinNumero,
                            commentaireAgent = p.commentaireAgent ?: row.commentaireAgent
                        )
                    )
                }
            }
        }

        if (dao.countAvis() == 0 && catalog.avis.isNotEmpty()) {
            dao.insertAvisList(
                catalog.avis.map {
                    Avis(
                        prestataireId = it.prestataireId,
                        auteurNom = it.auteurNom,
                        note = it.note.coerceIn(1, 5),
                        commentaire = it.commentaire
                    )
                }
            )
        }
    }

    private suspend fun mergeRemotePhotos() {
        if (!networkAllowed()) return
        val remote = runCatching { api.getPhotoUrls() }.getOrNull() ?: return
        remote.forEach { (key, dto) ->
            val id = key.toLongOrNull() ?: return@forEach
            val url = dto.url?.takeIf { it.startsWith("http") } ?: return@forEach
            val p = dao.findPrestataireById(id) ?: return@forEach
            val current = p.photoPath
            val keepLocalFile = !current.isNullOrBlank() &&
                !current.startsWith("http") &&
                File(current).exists()
            if (!keepLocalFile && current != url) {
                dao.updatePrestataire(p.copy(photoPath = url))
            }
        }
    }

    private suspend fun mergeRemoteDecisions() {
        if (!networkAllowed()) return
        runCatching { api.getDossiers() }.getOrNull().orEmpty().forEach { (key, dto) ->
            val id = dto.id.takeIf { it > 0 } ?: key.toLongOrNull() ?: return@forEach
            if (dto.status.isBlank()) return@forEach
            val p = dao.findPrestataireById(id)
                ?: dto.telephone.takeIf { it.isNotBlank() }?.let { dao.findPrestataireByTelephone(it) }
                ?: return@forEach
            dao.updatePrestataire(
                p.copy(
                    certificationStatus = dto.status,
                    commentaireAgent = dto.commentaire ?: p.commentaireAgent,
                    cinNumero = dto.cinNumero ?: p.cinNumero
                )
            )
        }
        runCatching { api.getMetiersProposes() }.getOrNull().orEmpty().forEach { (key, dto) ->
            val id = dto.id.takeIf { it > 0 } ?: key.toLongOrNull() ?: return@forEach
            if (dto.status.isBlank()) return@forEach
            val existing = dao.findMetierById(id)
            if (existing != null) {
                dao.updateMetier(existing.copy(status = dto.status))
            } else if (dto.nom.isNotBlank()) {
                dao.upsertMetier(
                    Metier(
                        id = id,
                        nom = dto.nom,
                        status = dto.status
                    )
                )
            }
        }
    }

    suspend fun publishDossier(prestataire: Prestataire) {
        if (!networkAllowed()) return
        val dto = DossierDto(
            id = prestataire.id,
            nom = prestataire.nom,
            telephone = prestataire.telephone,
            metierId = prestataire.metierId,
            metierNom = dao.metierName(prestataire.metierId).orEmpty(),
            communeId = prestataire.communeId,
            quartiers = dao.quartierNamesFor(prestataire.id).joinToString(", "),
            cinNumero = prestataire.cinNumero,
            cinRectoUrl = httpUrl(prestataire.cinRectoPath),
            cinVersoUrl = httpUrl(prestataire.cinVersoPath),
            status = prestataire.certificationStatus,
            commentaire = prestataire.commentaireAgent,
            updatedAt = System.currentTimeMillis()
        )
        runCatching { api.putDossier(prestataire.id, dto) }
            .onFailure { Log.w(TAG, "Dossier en ligne: ${it.message}") }
        val patch = CertificationPatchDto(
            certificationStatus = prestataire.certificationStatus,
            commentaireAgent = prestataire.commentaireAgent
        )
        val list = runCatching { api.getPrestataires() }.getOrNull()
            ?: runCatching { api.getCatalogPrestataires() }.getOrNull()
            ?: emptyList()
        val index = list.indexOfFirst { it.id == prestataire.id }
        if (index >= 0) {
            runCatching { api.patchPrestataire(index, patch) }
                .recoverCatching { api.patchCatalogPrestataire(index, patch) }
                .onFailure { Log.w(TAG, "Catalogue prestataire: ${it.message}") }
        }
    }

    suspend fun publishMetierPropose(metier: Metier, proposePar: String) {
        if (!networkAllowed()) return
        val dto = MetierProposeDto(
            id = metier.id,
            nom = metier.nom,
            status = metier.status,
            proposePar = proposePar,
            updatedAt = System.currentTimeMillis()
        )
        runCatching { api.putMetierPropose(metier.id, dto) }
            .onFailure { Log.w(TAG, "Métier proposé en ligne: ${it.message}") }
    }

    private fun httpUrl(path: String?): String? =
        path?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    companion object {
        private const val TAG = "CatalogSync"
    }
}
