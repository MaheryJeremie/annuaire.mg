package mg.annuaire.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mg.annuaire.app.data.model.Avis
import mg.annuaire.app.data.model.Commune
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.data.model.PrestataireQuartier
import mg.annuaire.app.data.model.Quartier
import mg.annuaire.app.data.model.Tarif
import mg.annuaire.app.data.model.User

@Dao
interface AnnuaireDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetiers(items: List<Metier>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetier(item: Metier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommunes(items: List<Commune>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuartiers(items: List<Quartier>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrestataire(prestataire: Prestataire)

    @Insert
    suspend fun insertPrestataire(prestataire: Prestataire): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestataireQuartiers(items: List<PrestataireQuartier>)

    @Insert
    suspend fun insertTarifs(items: List<Tarif>)

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updatePrestataire(prestataire: Prestataire)

    @Update
    suspend fun updateMetier(metier: Metier)

    @Query("SELECT COUNT(*) FROM communes")
    suspend fun countCommunes(): Int

    @Query("SELECT COUNT(*) FROM metiers")
    suspend fun countMetiers(): Int

    @Query("SELECT IFNULL(MAX(id), 0) FROM metiers")
    suspend fun maxMetierId(): Long

    @Query("SELECT * FROM metiers ORDER BY nom")
    fun observeMetiers(): Flow<List<Metier>>

    @Query("SELECT * FROM metiers WHERE status = 'APPROVED' ORDER BY nom")
    fun observeApprovedMetiers(): Flow<List<Metier>>

    @Query("SELECT * FROM metiers WHERE status = 'PENDING' ORDER BY nom")
    fun observePendingMetiers(): Flow<List<Metier>>

    @Query("SELECT COUNT(*) FROM metiers WHERE status = 'PENDING'")
    fun observePendingMetierCount(): Flow<Int>

    @Query("SELECT * FROM metiers WHERE LOWER(nom) = LOWER(:nom) LIMIT 1")
    suspend fun findMetierByName(nom: String): Metier?

    @Query("SELECT * FROM metiers WHERE id = :id LIMIT 1")
    suspend fun findMetierById(id: Long): Metier?

    @Query("SELECT * FROM metiers WHERE status = 'APPROVED' ORDER BY id LIMIT 1")
    suspend fun firstApprovedMetier(): Metier?

    @Query("SELECT * FROM communes ORDER BY nom")
    fun observeCommunes(): Flow<List<Commune>>

    @Query("SELECT * FROM communes WHERE id = :id LIMIT 1")
    suspend fun findCommune(id: Long): Commune?

    @Query("SELECT * FROM quartiers ORDER BY nom")
    fun observeQuartiers(): Flow<List<Quartier>>

    @Query("SELECT * FROM quartiers WHERE communeId = :communeId ORDER BY nom")
    fun observeQuartiersByCommune(communeId: Long): Flow<List<Quartier>>

    @Query("SELECT * FROM users WHERE telephone = :telephone LIMIT 1")
    suspend fun findUserByPhone(telephone: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findUserById(id: Long): User?

    @Query("SELECT * FROM prestataires WHERE userId = :userId LIMIT 1")
    suspend fun findPrestataireByUserId(userId: Long): Prestataire?

    @Query("SELECT * FROM prestataires WHERE id = :id LIMIT 1")
    suspend fun findPrestataireById(id: Long): Prestataire?

    @Query("SELECT * FROM prestataires WHERE telephone = :telephone LIMIT 1")
    suspend fun findPrestataireByTelephone(telephone: String): Prestataire?

    @Query("SELECT nom FROM metiers WHERE id = :id LIMIT 1")
    suspend fun metierName(id: Long): String?

    @Query("SELECT status FROM metiers WHERE id = :id LIMIT 1")
    suspend fun metierStatus(id: Long): String?

    @Query("SELECT nom FROM communes WHERE id = :id LIMIT 1")
    suspend fun communeName(id: Long): String?

    @Query(
        """
        SELECT q.nom FROM quartiers q
        INNER JOIN prestataire_quartiers pq ON pq.quartierId = q.id
        WHERE pq.prestataireId = :prestataireId
        ORDER BY q.nom
        """
    )
    suspend fun quartierNamesFor(prestataireId: Long): List<String>

    @Query(
        """
        SELECT q.* FROM quartiers q
        INNER JOIN prestataire_quartiers pq ON pq.quartierId = q.id
        WHERE pq.prestataireId = :prestataireId
        ORDER BY q.nom
        """
    )
    suspend fun quartiersFor(prestataireId: Long): List<Quartier>

    @Query("SELECT * FROM quartiers WHERE id IN (:ids)")
    suspend fun findQuartiersByIds(ids: List<Long>): List<Quartier>

    @Query("SELECT * FROM tarifs WHERE prestataireId = :prestataireId ORDER BY montantAr")
    suspend fun tarifsFor(prestataireId: Long): List<Tarif>

    @Query("DELETE FROM tarifs WHERE prestataireId = :prestataireId")
    suspend fun clearTarifs(prestataireId: Long)

    @Query("DELETE FROM prestataire_quartiers WHERE prestataireId = :prestataireId")
    suspend fun clearQuartiers(prestataireId: Long)

    @Query("SELECT * FROM prestataires WHERE certificationStatus = 'PENDING' ORDER BY id DESC")
    fun observePendingPrestataires(): Flow<List<Prestataire>>

    @Query("SELECT COUNT(*) FROM prestataires WHERE certificationStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prestataires WHERE certificationStatus = 'CERTIFIED'")
    fun observeCertifiedCount(): Flow<Int>

    @Insert
    suspend fun insertAvis(avis: Avis): Long

    @Insert
    suspend fun insertAvisList(items: List<Avis>)

    @Query("SELECT COUNT(*) FROM avis")
    suspend fun countAvis(): Int

    @Query("SELECT * FROM avis WHERE prestataireId = :prestataireId ORDER BY createdAt DESC")
    fun observeAvis(prestataireId: Long): Flow<List<Avis>>

    @Query("SELECT * FROM avis WHERE prestataireId = :prestataireId ORDER BY createdAt DESC")
    suspend fun avisFor(prestataireId: Long): List<Avis>

    @Query(
        """
        SELECT
            p.id AS id,
            p.nom AS nom,
            p.telephone AS telephone,
            m.nom AS metierNom,
            p.description AS description,
            p.disponibleAujourdhui AS disponibleAujourdhui,
            p.certificationStatus AS certificationStatus,
            c.nom AS commune,
            (
                SELECT MIN(t.montantAr) FROM tarifs t WHERE t.prestataireId = p.id
            ) AS tarifIndicatif,
            (
                SELECT GROUP_CONCAT(q.nom, ', ')
                FROM quartiers q
                INNER JOIN prestataire_quartiers pq ON pq.quartierId = q.id
                WHERE pq.prestataireId = p.id
            ) AS quartiers,
            p.photoPath AS photoPath,
            (
                SELECT AVG(a.note * 1.0) FROM avis a WHERE a.prestataireId = p.id
            ) AS noteMoyenne,
            (
                SELECT COUNT(*) FROM avis a WHERE a.prestataireId = p.id
            ) AS nbAvis
        FROM prestataires p
        INNER JOIN metiers m ON m.id = p.metierId
        INNER JOIN communes c ON c.id = p.communeId
        WHERE (:metierId = 0 OR p.metierId = :metierId)
          AND (:communeId = 0 OR p.communeId = :communeId)
          AND (
            :quartierId = 0 OR EXISTS (
                SELECT 1 FROM prestataire_quartiers pq2
                WHERE pq2.prestataireId = p.id AND pq2.quartierId = :quartierId
            )
          )
          AND (:certifiedOnly = 0 OR p.certificationStatus = 'CERTIFIED')
          AND (:availableOnly = 0 OR p.disponibleAujourdhui = 1)
        ORDER BY
            CASE p.certificationStatus WHEN 'CERTIFIED' THEN 0 WHEN 'PENDING' THEN 1 ELSE 2 END,
            IFNULL((SELECT AVG(a.note * 1.0) FROM avis a WHERE a.prestataireId = p.id), 0) DESC,
            p.nom
        """
    )
    fun searchPrestataires(
        metierId: Long,
        communeId: Long,
        quartierId: Long,
        certifiedOnly: Int,
        availableOnly: Int
    ): Flow<List<mg.annuaire.app.data.model.PrestataireListItem>>

    @Transaction
    suspend fun replaceReferenceData(
        metiers: List<Metier>,
        communes: List<Commune>,
        quartiers: List<Quartier>
    ) {
        upsertMetiers(metiers)
        upsertCommunes(communes)
        upsertQuartiers(quartiers)
    }
}
