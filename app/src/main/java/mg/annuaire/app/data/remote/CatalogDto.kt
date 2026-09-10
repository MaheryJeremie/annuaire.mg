package mg.annuaire.app.data.remote

data class CatalogDto(
    val version: Int = 1,
    val updatedAt: String? = null,
    val region: String? = null,
    val metiers: List<MetierDto> = emptyList(),
    val communes: List<CommuneDto> = emptyList(),
    val quartiers: List<QuartierDto> = emptyList(),
    val prestataires: List<PrestataireDto> = emptyList(),
    val usersDemo: List<UserDemoDto> = emptyList(),
    val avis: List<AvisDto> = emptyList()
)

data class MetierDto(
    val id: Long,
    val nom: String,
    val status: String = "APPROVED"
)

data class CommuneDto(
    val id: Long,
    val nom: String,
    val district: String,
    val type: String = "Urbaine"
)

data class QuartierDto(
    val id: Long,
    val communeId: Long,
    val nom: String
)

data class PrestataireDto(
    val id: Long,
    val nom: String,
    val telephone: String,
    val metierId: Long,
    val communeId: Long,
    val description: String = "",
    val disponibleAujourdhui: Boolean = true,
    val certificationStatus: String = "NONE",
    val cinNumero: String? = null,
    val patente: String? = null,
    val commentaireAgent: String? = null,
    val quartierIds: List<Long> = emptyList(),
    val tarifs: List<TarifDto> = emptyList(),
    val linkedDemoUser: Boolean = false
)

data class TarifDto(
    val libelle: String,
    val montantAr: Int
)

data class UserDemoDto(
    val nom: String,
    val telephone: String,
    val password: String,
    val role: String,
    val communeId: Long? = null
)

data class AvisDto(
    val prestataireId: Long,
    val auteurNom: String,
    val note: Int,
    val commentaire: String = ""
)

data class PhotoUrlDto(
    val url: String? = null
)

data class DossierDto(
    val id: Long = 0,
    val nom: String = "",
    val telephone: String = "",
    val metierId: Long = 0,
    val metierNom: String = "",
    val communeId: Long = 0,
    val quartiers: String = "",
    val cinNumero: String? = null,
    val cinRectoUrl: String? = null,
    val cinVersoUrl: String? = null,
    val status: String = "PENDING",
    val commentaire: String? = null,
    val updatedAt: Long = 0
)

data class MetierProposeDto(
    val id: Long = 0,
    val nom: String = "",
    val status: String = "PENDING",
    val proposePar: String = "",
    val updatedAt: Long = 0
)

data class CertificationPatchDto(
    val certificationStatus: String,
    val commentaireAgent: String? = null
)
