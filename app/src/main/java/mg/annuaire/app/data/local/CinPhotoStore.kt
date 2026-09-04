package mg.annuaire.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

object PhotoStore {
    fun save(context: Context, source: Uri, folder: String, prefix: String): String {
        val dir = File(context.filesDir, folder).apply { mkdirs() }
        val dest = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            if (bitmap != null) {
                val scaled = scaleDown(bitmap, 1280)
                dest.outputStream().use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
                }
                if (scaled !== bitmap) bitmap.recycle()
            } else {
                context.contentResolver.openInputStream(source)?.use { raw ->
                    dest.outputStream().use { output -> raw.copyTo(output) }
                } ?: error("Impossible de lire la photo.")
            }
        } ?: error("Impossible de lire la photo.")
        return dest.absolutePath
    }

    fun coilModel(path: String?): Any? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http://") || path.startsWith("https://")) path else File(path)
    }

    private fun scaleDown(source: Bitmap, maxSide: Int): Bitmap {
        val w = source.width
        val h = source.height
        val longest = maxOf(w, h)
        if (longest <= maxSide) return source
        val ratio = maxSide.toFloat() / longest
        return Bitmap.createScaledBitmap(source, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}

object CinPhotoStore {
    fun save(context: Context, source: Uri, side: String): String =
        PhotoStore.save(context, source, "cin", side)
}
