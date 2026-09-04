package mg.annuaire.app.data.repository

import kotlinx.coroutines.flow.Flow
import mg.annuaire.app.data.local.AnnuaireDao
import mg.annuaire.app.data.model.Avis
import mg.annuaire.app.data.model.CertificationStatus
import mg.annuaire.app.data.model.Commune
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.MetierStatus
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.data.model.PrestataireDetail
import mg.annuaire.app.data.model.PrestataireListItem
import mg.annuaire.app.data.model.PrestataireQuartier
import mg.annuaire.app.data.model.Quartier
import mg.annuaire.app.data.model.Tarif
import mg.annuaire.app.data.model.User
import mg.annuaire.app.data.model.UserRole
import mg.annuaire.app.data.sync.CatalogSync
import mg.annuaire.app.data.remote.PhotoCdn

class AnnuaireRepository(
    private val dao: AnnuaireDao,
    private val catalogSync: CatalogSync,
    private val photoCdn: PhotoCdn
) {

    suspend fun syncCatalog(): CatalogSync.Result = catalogSync.sync()

    fun observeMetiers(): Flow<List<Metier>> = dao.observeMetiers()
    fun observeApprovedMetiers(): Flow<List<Metier>> = dao.observeApprovedMetiers()
    fun observePendingMetiers(): Flow<List<Metier>> = dao.observePendingMetiers()
    fun observePendingMetierCount(): Flow<Int> = dao.observePendingMetierCount()
    fun observeCommunes(): Flow<List<Commune>> = dao.observeCommunes()
    fun observeQuartiers(): Flow<List<Quartier>> = dao.observeQuartiers()
    fun observeQuartiersByCommune(communeId: Long?): Flow<List<Quartier>> =
        if (communeId == null || communeId == 0L) dao.observeQuartiers()
        else dao.observeQuartiersByCommune(communeId)

    fun search(
        metierId: Long?,
        communeId: Long?,
        quartierId: Long?,
        certifiedOnly: Boolean,
        availableOnly: Boolean
    ): Flow<List<PrestataireListItem>> = dao.searchPrestataires(
        metierId = metierId ?: 0L,
        communeId = communeId ?: 0L,
        quartierId = quartierId ?: 0L,
        certifiedOnly = if (certifiedOnly) 1 else 0,
        availableOnly = if (availableOnly) 1 else 0
    )

    suspend fun getDetail(prestataireId: Long): PrestataireDetail? {
        val p = dao.findPrestataireById(prestataireId) ?: return null
        return PrestataireDetail(
            prestataire = p,
            metierNom = dao.metierName(p.metierId).orEmpty(),
            metierStatus = dao.metierStatus(p.metierId).orEmpty(),
            communeNom = dao.communeName(p.communeId).orEmpty(),
            quartiers = dao.quartierNamesFor(prestataireId),
            tarifs = dao.tarifsFor(prestataireId),
            avis = dao.avisFor(prestataireId)
        )
    }

    suspend fun registerProvider(nom: String, telephone: String, password: String): Result<User> {
        if (nom.trim().length < 2) {
            return Result.failure(IllegalStateException("Indiquez votre nom."))
        }
        if (telephone.trim().length < 8) {
            return Result.failure(IllegalStateException("Numéro de téléphone invalide."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalStateException("Mot de passe trop court."))
        }
        if (dao.findUserByPhone(telephone) != null) {
            return Result.failure(IllegalStateException("Ce téléphone est déjà utilisé."))
        }
        val defaultMetierId = dao.firstApprovedMetier()?.id
            ?: return Result.failure(IllegalStateException("Métiers non initialisés. Relancez l'app."))
        val userId = dao.insertUser(
            User(
                nom = nom.trim(),
                telephone = telephone.trim(),
                password = password,
                role = UserRole.PROVIDER.name,
                communeId = 1L
            )
        )
        dao.insertPrestataire(
            Prestataire(
                userId = userId,
                nom = nom.trim(),
                telephone = telephone.trim(),
                metierId = defaultMetierId,
                communeId = 1L,
                description = "",
                certificationStatus = CertificationStatus.NONE.name
            )
        )
        return Result.success(dao.findUserById(userId)!!)
    }

    suspend fun login(telephone: String, password: String): Result<User> {
        val user = dao.findUserByPhone(telephone.trim())
            ?: return Result.failure(IllegalStateException("Compte introuvable."))
        if (user.password != password) {
            return Result.failure(IllegalStateException("Mot de passe incorrect."))
        }
        if (user.role == UserRole.AGENT.name) {
            return Result.failure(
                IllegalStateException("Cet accès est réservé à l’outil web de la commune, pas à l’application mobile.")
            )
        }
        return Result.success(user)
    }

    suspend fun getPrestataireForUser(userId: Long): Prestataire? =
        dao.findPrestataireByUserId(userId)

    suspend fun getMetier(id: Long): Metier? = dao.findMetierById(id)

    suspend fun saveProviderProfile(
        prestataire: Prestataire,
        quartierIds: List<Long>,
        tarifs: List<Pair<String, Int>>
    ) {
        val remotePhoto = photoCdn.publishProfilePhoto(prestataire.id, prestataire.photoPath)
        dao.updatePrestataire(prestataire.copy(photoPath = remotePhoto ?: prestataire.photoPath))
        dao.clearQuartiers(prestataire.id)
        dao.insertPrestataireQuartiers(
            quartierIds.map { PrestataireQuartier(prestataire.id, it) }
        )
        dao.clearTarifs(prestataire.id)
        dao.insertTarifs(
            tarifs.filter { it.first.isNotBlank() }.map {
                Tarif(prestataireId = prestataire.id, libelle = it.first, montantAr = it.second)
            }
        )
    }

    /**
     * Choisit un métier existant, ou propose un nouveau nom à valider par la commune.
     */
    suspend fun resolveOrProposeMetier(nom: String, userId: Long): Result<Metier> {
        val clean = nom.trim()
        if (clean.length < 3) {
            return Result.failure(IllegalStateException("Le nom du métier est trop court."))
        }
        val existing = dao.findMetierByName(clean)
        if (existing != null) {
            if (existing.status == MetierStatus.REJECTED.name) {
                return Result.failure(IllegalStateException("Ce métier a été refusé. Choisissez-en un autre."))
            }
            return Result.success(existing)
        }
        val metier = Metier(
            id = dao.maxMetierId() + 1,
            nom = clean.replaceFirstChar { it.uppercase() },
            status = MetierStatus.PENDING.name,
            proposedByUserId = userId
        )
        dao.upsertMetier(metier)
        if (metier.status == MetierStatus.PENDING.name) {
            val proposePar = dao.findUserById(userId)?.nom.orEmpty()
            catalogSync.publishMetierPropose(metier, proposePar)
        }
        return Result.success(metier)
    }

    suspend fun decideMetier(metierId: Long, approve: Boolean) {
        val m = dao.findMetierById(metierId) ?: return
        dao.updateMetier(
            m.copy(
                status = if (approve) MetierStatus.APPROVED.name else MetierStatus.REJECTED.name
            )
        )
    }

    suspend fun requestCertification(
        prestataireId: Long,
        communeId: Long,
        cinNumero: String,
        cinRectoPath: String?,
        cinVersoPath: String?
    ): Result<Unit> {
        val p = dao.findPrestataireById(prestataireId)
            ?: return Result.failure(IllegalStateException("Profil introuvable."))
        if (p.description.isBlank()) {
            return Result.failure(IllegalStateException("Complétez d'abord votre fiche (description)."))
        }
        val cin = cinNumero.trim()
        if (cin.length < 5) {
            return Result.failure(IllegalStateException("Indiquez le numéro de CIN."))
        }
        if (cinRectoPath.isNullOrBlank() || cinVersoPath.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Ajoutez les photos CIN recto et verso."))
        }
        dao.updatePrestataire(
            p.copy(
                communeId = communeId,
                cinNumero = cin,
                cinRectoPath = cinRectoPath,
                cinVersoPath = cinVersoPath,
                certificationStatus = CertificationStatus.PENDING.name,
                commentaireAgent = null
            )
        )
        dao.findPrestataireById(prestataireId)?.let { catalogSync.publishDossier(it) }
        return Result.success(Unit)
    }

    fun observePending(): Flow<List<Prestataire>> = dao.observePendingPrestataires()
    fun observePendingCount(): Flow<Int> = dao.observePendingCount()
    fun observeCertifiedCount(): Flow<Int> = dao.observeCertifiedCount()

    fun observeAvis(prestataireId: Long): Flow<List<Avis>> = dao.observeAvis(prestataireId)

    suspend fun addAvis(
        prestataireId: Long,
        auteurNom: String,
        note: Int,
        commentaire: String
    ): Result<Unit> {
        if (dao.findPrestataireById(prestataireId) == null) {
            return Result.failure(IllegalStateException("Prestataire introuvable."))
        }
        val nom = auteurNom.trim()
        if (nom.length < 2) {
            return Result.failure(IllegalStateException("Indiquez votre nom."))
        }
        if (note !in 1..5) {
            return Result.failure(IllegalStateException("Choisissez une note de 1 à 5."))
        }
        dao.insertAvis(
            Avis(
                prestataireId = prestataireId,
                auteurNom = nom,
                note = note,
                commentaire = commentaire.trim()
            )
        )
        return Result.success(Unit)
    }

    suspend fun decideCertification(
        prestataireId: Long,
        approve: Boolean,
        comment: String
    ) {
        val p = dao.findPrestataireById(prestataireId) ?: return
        dao.updatePrestataire(
            p.copy(
                certificationStatus = if (approve) {
                    CertificationStatus.CERTIFIED.name
                } else {
                    CertificationStatus.REJECTED.name
                },
                commentaireAgent = comment.ifBlank { null }
            )
        )
    }
}
