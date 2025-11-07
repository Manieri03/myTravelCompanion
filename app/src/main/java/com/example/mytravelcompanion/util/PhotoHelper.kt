package com.example.mytravelcompanion.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object PhotoHelper {

    fun compressImage(context: Context, originalPath: String): String {
        try {
            val originalFile = File(originalPath)
            val bitmap = BitmapFactory.decodeFile(originalPath)
            val exif = ExifInterface(originalPath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            val compressedFile = File(context.filesDir, "compressed_${originalFile.name}")
            FileOutputStream(compressedFile).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
            }

            if (originalFile.absolutePath.startsWith(context.filesDir.absolutePath)) {
                originalFile.delete()
            }

            return compressedFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            return originalPath
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
