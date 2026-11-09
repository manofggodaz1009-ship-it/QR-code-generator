package com.example.generator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.DotStyle
import com.example.model.LogoShape
import com.example.model.PresetLogo
import com.example.model.QrConfig
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object QrCodeGenerator {

    fun generateQrBitmap(context: Context, config: QrConfig): Bitmap? {
        if (config.content.isBlank()) return null

        val hasLogo = config.customLogoUri != null || config.presetLogo != PresetLogo.NONE
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            // Crucial: Use H (30% error correction) when logo is embedded so it remains 100% scannable
            put(
                EncodeHintType.ERROR_CORRECTION,
                if (hasLogo) ErrorCorrectionLevel.H else ErrorCorrectionLevel.M
            )
            put(EncodeHintType.MARGIN, 3)
        }

        return try {
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(
                config.content,
                BarcodeFormat.QR_CODE,
                config.size,
                config.size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill background
            val bgPaint = Paint().apply {
                color = config.backgroundColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Calculate module dimensions
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            // Find top-left module
            var minX = matrixWidth
            var minY = matrixHeight
            var maxX = 0
            var maxY = 0
            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (bitMatrix.get(x, y)) {
                        if (x < minX) minX = x
                        if (y < minY) minY = y
                        if (x > maxX) maxX = x
                        if (y > maxY) maxY = y
                    }
                }
            }

            val moduleCount = (maxX - minX + 1)
            val moduleSize = if (moduleCount > 0) (width.toFloat() - (minX * 2f * (width.toFloat() / matrixWidth))) / moduleCount else width.toFloat() / matrixWidth
            val leftOffset = (width - (matrixWidth * (width.toFloat() / matrixWidth))) / 2f
            val topOffset = (height - (matrixHeight * (height.toFloat() / matrixHeight))) / 2f

            val fgPaint = Paint().apply {
                color = config.foregroundColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val cellW = width.toFloat() / matrixWidth
            val cellH = height.toFloat() / matrixHeight

            // Check if cell is part of Finder Pattern (the 3 corner squares: 7x7 modules)
            fun isFinderPattern(x: Int, y: Int): Boolean {
                val inTopLeft = (x in minX..(minX + 6)) && (y in minY..(minY + 6))
                val inTopRight = (x in (maxX - 6)..maxX) && (y in minY..(minY + 6))
                val inBottomLeft = (x in minX..(minX + 6)) && (y in (maxY - 6)..maxY)
                return inTopLeft || inTopRight || inBottomLeft
            }

            // Draw QR modules
            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (bitMatrix.get(x, y)) {
                        val left = x * cellW
                        val top = y * cellH
                        val right = left + cellW
                        val bottom = top + cellH

                        if (isFinderPattern(x, y)) {
                            // Finder patterns: render sharp/semi-rounded for maximum contrast and rapid detection
                            val cornerRadius = cellW * 0.25f
                            canvas.drawRoundRect(RectF(left, top, right, bottom), cornerRadius, cornerRadius, fgPaint)
                        } else {
                            when (config.dotStyle) {
                                DotStyle.SQUARE -> {
                                    canvas.drawRect(left, top, right, bottom, fgPaint)
                                }
                                DotStyle.ROUNDED -> {
                                    val radius = cellW * 0.4f
                                    canvas.drawRoundRect(RectF(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f), radius, radius, fgPaint)
                                }
                                DotStyle.CIRCLE -> {
                                    val radius = (cellW / 2f) * 0.9f
                                    canvas.drawCircle(left + cellW / 2f, top + cellH / 2f, radius, fgPaint)
                                }
                            }
                        }
                    }
                }
            }

            // Embed Logo if enabled
            if (hasLogo) {
                val logoBitmap = loadLogoBitmap(context, config)
                if (logoBitmap != null) {
                    embedLogoOnQr(canvas, width, height, logoBitmap, config)
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun embedLogoOnQr(
        canvas: Canvas,
        qrWidth: Int,
        qrHeight: Int,
        logoBitmap: Bitmap,
        config: QrConfig
    ) {
        // Safe logo size ratio: 12% to 28% of total QR code size
        val safeRatio = config.logoSizeRatio.coerceIn(0.12f, 0.28f)
        val logoBoxSize = (qrWidth * safeRatio).toInt()

        val centerX = qrWidth / 2f
        val centerY = (qrHeight / 2f) + (qrHeight * config.logoOffsetYRatio)

        val left = centerX - (logoBoxSize / 2f)
        val top = centerY - (logoBoxSize / 2f)
        val right = left + logoBoxSize
        val bottom = top + logoBoxSize
        val badgeRect = RectF(left, top, right, bottom)

        // Draw badge background to isolate logo from QR cells (essential for scanner readability)
        if (config.logoShape != LogoShape.NONE) {
            val bgPaint = Paint().apply {
                color = config.logoBgColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            when (config.logoShape) {
                LogoShape.CIRCLE -> {
                    canvas.drawCircle(centerX, centerY, logoBoxSize / 2f, bgPaint)
                }
                LogoShape.ROUNDED_RECT -> {
                    val cornerRadius = logoBoxSize * 0.25f
                    canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, bgPaint)
                }
                LogoShape.SQUARE -> {
                    canvas.drawRect(badgeRect, bgPaint)
                }
                LogoShape.NONE -> {
                    // No background
                }
            }
        }

        // Draw the inner logo bitmap with padding
        val padding = logoBoxSize * config.logoPaddingFraction.coerceIn(0.04f, 0.25f)
        val innerRect = RectF(
            left + padding,
            top + padding,
            right - padding,
            bottom - padding
        )

        // Shape clip the logo image if requested
        val processedLogo = if (config.logoShape == LogoShape.CIRCLE) {
            getCroppedCircleBitmap(logoBitmap)
        } else {
            logoBitmap
        }

        val destRect = Rect(
            innerRect.left.toInt(),
            innerRect.top.toInt(),
            innerRect.right.toInt(),
            innerRect.bottom.toInt()
        )
        val srcRect = Rect(0, 0, processedLogo.width, processedLogo.height)
        val logoPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        canvas.drawBitmap(processedLogo, srcRect, destRect, logoPaint)
    }

    private fun getCroppedCircleBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val srcLeft = (bitmap.width - size) / 2
        val srcTop = (bitmap.height - size) / 2
        val srcRect = Rect(srcLeft, srcTop, srcLeft + size, srcTop + size)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        return output
    }

    private fun loadLogoBitmap(context: Context, config: QrConfig): Bitmap? {
        // Priority 1: User custom selected image from gallery
        if (config.customLogoUri != null) {
            return try {
                context.contentResolver.openInputStream(config.customLogoUri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        // Priority 2: Built-in preset logos
        return when (config.presetLogo) {
            PresetLogo.RUPEE -> createRupeeLogoBitmap()
            PresetLogo.GPAY -> createGpayLogoBitmap()
            PresetLogo.PHONEPE -> createPhonePeLogoBitmap()
            PresetLogo.PAYTM -> createPaytmLogoBitmap()
            PresetLogo.WHATSAPP -> createWhatsAppLogoBitmap()
            PresetLogo.WIFI -> createWifiLogoBitmap()
            PresetLogo.WEB -> createWebLogoBitmap()
            PresetLogo.CALL -> createCallLogoBitmap()
            PresetLogo.HEART -> createHeartLogoBitmap()
            PresetLogo.SHIELD -> createShieldLogoBitmap()
            PresetLogo.NONE -> null
        }
    }

    // High quality vector-drawn preset logo bitmaps
    private fun createRupeeLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFFD4AF37.toInt() // Gold Rupee
            textAlign = Paint.Align.CENTER
            textSize = size * 0.75f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("₹", size / 2f, yPos, paint)
        return bmp
    }

    private fun createGpayLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF1A73E8.toInt() // Google Blue
            textAlign = Paint.Align.CENTER
            textSize = size * 0.55f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("G", size / 2f, yPos, paint)
        return bmp
    }

    private fun createPhonePeLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF5F259F.toInt() // PhonePe Purple
            textAlign = Paint.Align.CENTER
            textSize = size * 0.55f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("पे", size / 2f, yPos, paint)
        return bmp
    }

    private fun createPaytmLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF00B9F1.toInt() // Paytm Cyan
            textAlign = Paint.Align.CENTER
            textSize = size * 0.45f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("Pay", size / 2f, yPos, paint)
        return bmp
    }

    private fun createWhatsAppLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF25D366.toInt() // WhatsApp Green
            textAlign = Paint.Align.CENTER
            textSize = size * 0.55f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("WA", size / 2f, yPos, paint)
        return bmp
    }

    private fun createWifiLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF1976D2.toInt()
            style = Paint.Style.STROKE
            strokeWidth = size * 0.08f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        val center = size / 2f
        canvas.drawArc(RectF(center - 80f, center - 40f, center + 80f, center + 120f), 220f, 100f, false, paint)
        canvas.drawArc(RectF(center - 50f, center - 10f, center + 50f, center + 90f), 220f, 100f, false, paint)
        val dotPaint = Paint().apply {
            color = 0xFF1976D2.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(center, center + 40f, size * 0.06f, dotPaint)
        return bmp
    }

    private fun createWebLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF0288D1.toInt()
            textAlign = Paint.Align.CENTER
            textSize = size * 0.6f
            isAntiAlias = true
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("🌐", size / 2f, yPos, paint)
        return bmp
    }

    private fun createCallLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFF388E3C.toInt()
            textAlign = Paint.Align.CENTER
            textSize = size * 0.6f
            isAntiAlias = true
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("📞", size / 2f, yPos, paint)
        return bmp
    }

    private fun createHeartLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFFE91E63.toInt()
            textAlign = Paint.Align.CENTER
            textSize = size * 0.6f
            isAntiAlias = true
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("❤", size / 2f, yPos, paint)
        return bmp
    }

    private fun createShieldLogoBitmap(): Bitmap {
        val size = 256
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = 0xFFD4AF37.toInt()
            textAlign = Paint.Align.CENTER
            textSize = size * 0.6f
            isAntiAlias = true
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("🛡", size / 2f, yPos, paint)
        return bmp
    }

    /**
     * Verifies whether the generated QR bitmap is 100% scannable by decoders.
     * Returns the decoded text string if valid, or null if unreadable.
     */
    fun verifyScannability(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = QRCodeReader()
            val result = reader.decode(bBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves the QR code Bitmap to device MediaStore (Pictures/CNV_QR)
     */
    fun saveQrToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "CNV_QR_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CNV_QR")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/CNV_QR"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
                imageUri = Uri.fromFile(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Writes bitmap to internal cache and returns a content Uri shareable via FileProvider
     */
    fun getShareableUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "qr_codes")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, "share_qr_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
