package mg.annuaire.app.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mg.annuaire.app.BuildConfig
import mg.annuaire.app.data.sync.WifiChecker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Photos de profil : fichier local + URL Cloudinary.
 * Les photos CIN ne passent jamais par ici.
 */
class PhotoCdn(
    private val context: Context,
    private val api: AnnuaireApi,
    private val http: OkHttpClient
) {
    suspend fun publishProfilePhoto(prestataireId: Long, path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) {
            runCatching { api.putPhotoUrl(prestataireId, PhotoUrlDto(path)) }
            return path
        }
        if (!WifiChecker.isOnline(context)) return null
        val file = File(path)
        if (!file.exists()) return null
        val url = uploadCloudinary(file) ?: return null
        runCatching { api.putPhotoUrl(prestataireId, PhotoUrlDto(url)) }
            .onFailure { Log.w(TAG, "Index photos: ${it.message}") }
        return url
    }

    private suspend fun uploadCloudinary(file: File): String? = withContext(Dispatchers.IO) {
        val cloud = BuildConfig.CDN_CLOUDINARY_CLOUD.trim()
        val preset = BuildConfig.CDN_CLOUDINARY_PRESET.trim()
        if (cloud.isEmpty() || preset.isEmpty()) {
            Log.w(TAG, "Cloudinary non configuré (local.properties).")
            return@withContext null
        }
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", preset)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
            .build()
        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloud/image/upload")
            .post(body)
            .build()
        http.newCall(request).execute().use { response ->
            val json = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.w(TAG, "Upload photo ${response.code}: $json")
                return@use null
            }
            json.stringField("secure_url") ?: json.stringField("url")
        }
    }

    companion object {
        private const val TAG = "PhotoCdn"
    }
}

private fun String.stringField(name: String): String? {
    val key = "\"$name\""
    val start = indexOf(key)
    if (start < 0) return null
    val colon = indexOf(':', start)
    val firstQuote = indexOf('"', colon + 1)
    val secondQuote = indexOf('"', firstQuote + 1)
    if (firstQuote < 0 || secondQuote < 0) return null
    return substring(firstQuote + 1, secondQuote)
        .replace("\\/", "/")
        .replace("\\u0026", "&")
}
