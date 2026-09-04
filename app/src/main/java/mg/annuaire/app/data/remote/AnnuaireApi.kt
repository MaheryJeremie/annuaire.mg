package mg.annuaire.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface AnnuaireApi {
    /** Nœud `catalog` (option A du guide). */
    @GET("catalog.json")
    suspend fun getCatalog(): CatalogDto

    /** Catalogue importé à la racine de la RTDB. */
    @GET(".json")
    suspend fun getRootCatalog(): CatalogDto

    @GET("prestataires.json")
    suspend fun getPrestataires(): List<PrestataireDto>?

    @GET("catalog/prestataires.json")
    suspend fun getCatalogPrestataires(): List<PrestataireDto>?

    @PATCH("prestataires/{index}.json")
    suspend fun patchPrestataire(@Path("index") index: Int, @Body body: CertificationPatchDto)

    @PATCH("catalog/prestataires/{index}.json")
    suspend fun patchCatalogPrestataire(@Path("index") index: Int, @Body body: CertificationPatchDto)

    @GET("dossiers.json")
    suspend fun getDossiers(): Map<String, DossierDto>?

    @PUT("dossiers/{id}.json")
    suspend fun putDossier(@Path("id") id: Long, @Body body: DossierDto)

    @GET("metiers_proposes.json")
    suspend fun getMetiersProposes(): Map<String, MetierProposeDto>?

    @PUT("metiers_proposes/{id}.json")
    suspend fun putMetierPropose(@Path("id") id: Long, @Body body: MetierProposeDto)

    @GET("photos.json")
    suspend fun getPhotoUrls(): Map<String, PhotoUrlDto>?

    @PUT("photos/{id}.json")
    suspend fun putPhotoUrl(@Path("id") id: Long, @Body body: PhotoUrlDto)
}
