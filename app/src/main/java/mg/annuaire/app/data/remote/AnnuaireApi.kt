package mg.annuaire.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AnnuaireApi {
    /** Firebase RTDB : .../catalog.json  |  Hosting statique : .../catalog.json */
    @GET("catalog.json")
    suspend fun getCatalog(): CatalogDto

    @GET("photos.json")
    suspend fun getPhotoUrls(): Map<String, PhotoUrlDto>?

    @PUT("photos/{id}.json")
    suspend fun putPhotoUrl(@Path("id") id: Long, @Body body: PhotoUrlDto)
}
