package mg.annuaire.app.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import mg.annuaire.app.BuildConfig
import mg.annuaire.app.data.session.SettingsStore
import mg.annuaire.app.data.sync.WifiChecker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Envoi des images (profil public, CIN pour la commune uniquement).
 * Rien de technique n’apparaît dans l’interface.
 */
class PhotoCdn(
    private val context: Context,
    private val api: AnnuaireApi,
    private val http: OkHttpClient,
    private val settings: SettingsStore
) {
    suspend fun publishProfilePhoto(prestataireId: Long, path: String?): String? {
        val url = publishFile(path) ?: return path?.takeIf { it.startsWith("http") }
        runCatching { api.putPhotoUrl(prestataireId, PhotoUrlDto(url)) }
            .onFailure { Log.w(TAG, "Index photos: ${it.message}") }
        return url
    }

    /** CIN : URL distante pour l’outil commune, jamais indexée dans `photos/`. */
    suspend fun publishCinPhoto(path: String?): String? = publishFile(path)

    private suspend fun publishFile(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        if (!WifiChecker.allows(context, settings.networkMode.first())) return null
        val file = File(path)
        if (!file.exists()) return null
        return uploadCloudinary(file)
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
