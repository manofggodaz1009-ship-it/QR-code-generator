package com.example.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class CameraQrAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var isScanning = true

    fun resume() {
        isScanning = true
    }

    fun pause() {
        isScanning = false
    }

    override fun analyze(image: ImageProxy) {
        if (!isScanning) {
            image.close()
            return
        }

        val planes = image.planes
        if (planes.isEmpty()) {
            image.close()
            return
        }

        val buffer = planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = image.width
        val height = image.height

        try {
            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(binaryBitmap)

            if (result != null && result.text.isNotBlank()) {
                isScanning = false
                onQrDetected(result.text)
            }
        } catch (_: Exception) {
            // Frame did not contain a readable barcode - continue scanning next frame
        } finally {
            image.close()
        }
    }
}
