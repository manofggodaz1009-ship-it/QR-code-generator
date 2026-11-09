package com.example.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

object QrGalleryDecoder {

    fun decodeFromUri(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            decodeFromBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(bBitmap)
            result.text
        } catch (e: Exception) {
            // Try scaled down if bitmap is very large
            try {
                if (bitmap.width > 1200 || bitmap.height > 1200) {
                    val scaled = Bitmap.createScaledBitmap(bitmap, 800, (800f * bitmap.height / bitmap.width).toInt(), true)
                    val w = scaled.width
                    val h = scaled.height
                    val px = IntArray(w * h)
                    scaled.getPixels(px, 0, w, 0, 0, w, h)
                    val src = RGBLuminanceSource(w, h, px)
                    val bb = BinaryBitmap(HybridBinarizer(src))
                    val reader = MultiFormatReader()
                    reader.decode(bb).text
                } else null
            } catch (ex: Exception) {
                null
            }
        }
    }
}
