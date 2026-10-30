package com.bloodnetwork.bangladesh.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * Turns whatever the user picked into the one thing the server accepts for a profile picture:
 * a small square JPEG. Doing it here means the user can choose PNG, JPEG, WebP, HEIC... without
 * caring about formats, and the upload stays a few dozen kilobytes on mobile data.
 */
object ImageProcessing {

    const val AVATAR_SIZE_PX = 512

    // Stay well under the server's 400 KB cap.
    private const val MAX_BYTES = 300 * 1024
    private val QUALITY_STEPS = intArrayOf(88, 80, 70, 60, 50)

    /** Square [AVATAR_SIZE_PX] JPEG of the picked image, or null if it can't be read as an image. */
    suspend fun toAvatarJpeg(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.Default) {
        runCatching { encode(context, uri) }.getOrNull()
    }

    private fun encode(context: Context, uri: Uri): ByteArray? {
        val resolver = context.contentResolver

        // Pass 1: only the size, so a 50-megapixel photo is never fully decoded.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // decodeStream returns null in bounds-only mode, so the size is read from `bounds`, not the result.
        val opened = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds); true } ?: return null
        if (!opened || bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Pass 2: decode at the largest power-of-two reduction that still has enough pixels.
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(min(bounds.outWidth, bounds.outHeight))
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null

        val upright = rotateByExif(context, uri, decoded)

        // Centre-crop to a square, then scale down to the target size.
        val side = min(upright.width, upright.height)
        val square = Bitmap.createBitmap(upright, (upright.width - side) / 2, (upright.height - side) / 2, side, side)
        val scaled = if (side > AVATAR_SIZE_PX) Bitmap.createScaledBitmap(square, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true) else square

        // JPEG has no transparency: put PNG/WebP cut-outs on white instead of letting them go black.
        val flat = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        Canvas(flat).apply {
            drawColor(Color.WHITE)
            drawBitmap(scaled, 0f, 0f, null)
        }

        val out = ByteArrayOutputStream()
        for (quality in QUALITY_STEPS) {
            out.reset()
            flat.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (out.size() <= MAX_BYTES) return out.toByteArray()
        }
        return null
    }

    private fun sampleSizeFor(shortSide: Int): Int {
        var sample = 1
        while (shortSide / (sample * 2) >= AVATAR_SIZE_PX) sample *= 2
        return sample
    }

    /** Phone cameras store sideways photos with an EXIF rotation flag; BitmapFactory ignores it. */
    private fun rotateByExif(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
