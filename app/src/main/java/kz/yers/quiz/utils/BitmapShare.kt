package kz.yers.quiz.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val SHARED_DIR = "shared"
private const val PROVIDER_SUFFIX = ".fileprovider"

suspend fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
): Uri =
    withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(
            context,
            context.packageName + PROVIDER_SUFFIX,
            file,
        )
    }

fun shareImage(
    context: Context,
    uri: Uri,
    captionText: String,
    chooserTitle: String,
) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, captionText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    val chooser =
        Intent.createChooser(send, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(chooser)
}
