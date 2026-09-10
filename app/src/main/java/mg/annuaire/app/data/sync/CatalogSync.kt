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
import mg.annuaire.app.data.remote.NetworkModule

/**
 * Offline-first : Room = source de vérité.
 * Sync **uniquement en Wi‑Fi** ; sinon (ou si erreur réseau) → assets/catalog.json.
 */
class CatalogSync(
    private val context: Context,
    private val dao: AnnuaireDao,
    private val api: AnnuaireApi
) {
    sealed class Result {
        data class Ok(val source: String, val communes: Int, val quartiers: Int) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun sync(): Result {
        val catalog = resolveCatalog()
        applyCatalog(catalog.first)
        mergeRemotePhotos()
        return Result.Ok(
            source = catalog.second,
            communes = catalog.first.communes.size,
            quartiers = catalog.first.quartiers.size
        )
    }

    private suspend fun resolveCatalog(): Pair<CatalogDto, String> {
        if (!WifiChecker.isWifiConnected(context)) {
            Log.i(TAG, "Pas de Wi‑Fi → pas de sync réseau, fallback assets")
            return loadAssets("hors Wi‑Fi · catalogue local")
        }
        return try {
            val remote = api.getCatalog()
            Log.i(TAG, "Sync Wi‑Fi OK (version=${remote.version})")
            remote to "Wi‑Fi · Firebase"
        } catch (e: Exception) {
            Log.w(TAG, "Sync Wi‑Fi échouée, fallback assets: ${e.message}")
            loadAssets("Wi‑Fi · catalogue local")
        }
    }

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
                        cinNumero = p.cinNumero ?: p.patente
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
            } else if (existing.userId == null && linkedUserId != null) {
                dao.updatePrestataire(existing.copy(userId = linkedUserId))
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
        if (!WifiChecker.isWifiConnected(context)) return
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

    companion object {
        private const val TAG = "CatalogSync"
    }
}
