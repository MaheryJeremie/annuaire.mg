package mg.annuaire.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val telephone: String,
    val password: String,
    val role: String,
    val communeId: Long? = null
)

@Entity(tableName = "metiers")
data class Metier(
    @PrimaryKey val id: Long,
    val nom: String,
    val status: String = MetierStatus.APPROVED.name,
    val proposedByUserId: Long? = null
)

@Entity(tableName = "communes")
data class Commune(
    @PrimaryKey val id: Long,
    val nom: String,
    val district: String,
    val type: String = "Urbaine"
)

@Entity(tableName = "quartiers")
data class Quartier(
    @PrimaryKey val id: Long,
    val communeId: Long,
    val nom: String
)

@Entity(tableName = "prestataires")
data class Prestataire(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val nom: String,
    val telephone: String,
    val metierId: Long,
    val communeId: Long,
    val description: String = "",
    val disponibleAujourdhui: Boolean = true,
    val certificationStatus: String = CertificationStatus.NONE.name,
    val cinNumero: String? = null,
    val cinRectoPath: String? = null,
    val cinVersoPath: String? = null,
    val commentaireAgent: String? = null,
    val photoPath: String? = null
)

@Entity(tableName = "avis")
data class Avis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prestataireId: Long,
    val auteurNom: String,
    val note: Int,
    val commentaire: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prestataire_quartiers", primaryKeys = ["prestataireId", "quartierId"])
data class PrestataireQuartier(
    val prestataireId: Long,
    val quartierId: Long
)

@Entity(tableName = "tarifs")
data class Tarif(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prestataireId: Long,
    val libelle: String,
    val montantAr: Int
)

data class PrestataireListItem(
    val id: Long,
    val nom: String,
    val telephone: String,
    val metierNom: String,
    val description: String,
    val disponibleAujourdhui: Boolean,
    val certificationStatus: String,
    val commune: String,
    val tarifIndicatif: Int?,
    val quartiers: String,
    val photoPath: String?,
    val noteMoyenne: Double?,
    val nbAvis: Int
)

data class PrestataireDetail(
    val prestataire: Prestataire,
    val metierNom: String,
    val metierStatus: String,
    val communeNom: String,
    val quartiers: List<String>,
    val tarifs: List<Tarif>,
    val avis: List<Avis> = emptyList()
)
