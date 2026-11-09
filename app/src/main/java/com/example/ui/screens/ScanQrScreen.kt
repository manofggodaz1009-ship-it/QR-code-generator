package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.QrHistoryDatabase
import com.example.data.QrHistoryEntity
import com.example.model.QrType
import com.example.scanner.CameraQrAnalyzer
import com.example.scanner.QrGalleryDecoder
import com.example.ui.components.UpiPaymentConfirmSheet
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.upi.UpiDetails
import com.example.upi.UpiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val db = remember { QrHistoryDatabase.getDatabase(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "கேமரா அனுமதி மறுக்கப்பட்டது. நீங்கள் கேலரி மூலம் ஸ்கேன் செய்யலாம்.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Scanned result state
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var detectedType by remember { mutableStateOf<QrType>(QrType.TEXT) }
    var detectedUpiDetails by remember { mutableStateOf<UpiDetails?>(null) }

    // Torch state
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }

    // Analyzer instance
    val analyzer = remember {
        CameraQrAnalyzer { resultText ->
            scope.launch(Dispatchers.Main) {
                // Vibrate on successful scan
                triggerHaptic(context)

                scannedContent = resultText
                val upi = UpiManager.parseUpiUri(resultText)
                if (upi != null) {
                    detectedType = QrType.UPI
                    detectedUpiDetails = upi
                } else if (resultText.startsWith("http://") || resultText.startsWith("https://")) {
                    detectedType = QrType.URL
                } else if (resultText.startsWith("WIFI:")) {
                    detectedType = QrType.WIFI
                } else if (resultText.startsWith("BEGIN:VCARD")) {
                    detectedType = QrType.CONTACT
                } else {
                    detectedType = QrType.TEXT
                }

                // Save to Room DB
                scope.launch(Dispatchers.IO) {
                    db.qrHistoryDao().insert(
                        QrHistoryEntity(
                            content = resultText,
                            title = when (detectedType) {
                                QrType.UPI -> "UPI: ${upi?.payeeName ?: "Payment"}"
                                QrType.URL -> "இணையதளம் (URL)"
                                QrType.WIFI -> "வைஃபை (Wi-Fi)"
                                QrType.CONTACT -> "தொடர்பு (vCard)"
                                else -> "உரை (Scanned Text)"
                            },
                            type = detectedType.name,
                            isGenerated = false,
                            upiAmount = upi?.amount,
                            upiVpa = upi?.vpa
                        )
                    )
                }
            }
        }
    }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val decoded = QrGalleryDecoder.decodeFromUri(context, uri)
                launch(Dispatchers.Main) {
                    if (decoded != null) {
                        triggerHaptic(context)
                        scannedContent = decoded
                        val upi = UpiManager.parseUpiUri(decoded)
                        if (upi != null) {
                            detectedType = QrType.UPI
                            detectedUpiDetails = upi
                        } else if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
                            detectedType = QrType.URL
                        } else if (decoded.startsWith("WIFI:")) {
                            detectedType = QrType.WIFI
                        } else {
                            detectedType = QrType.TEXT
                        }

                        // Save to Room DB
                        scope.launch(Dispatchers.IO) {
                            db.qrHistoryDao().insert(
                                QrHistoryEntity(
                                    content = decoded,
                                    title = if (detectedType == QrType.UPI) "UPI: ${upi?.payeeName ?: "Payment"}" else "கேலரி QR",
                                    type = detectedType.name,
                                    isGenerated = false,
                                    upiAmount = upi?.amount,
                                    upiVpa = upi?.vpa
                                )
                            )
                        }
                    } else {
                        Toast.makeText(context, "தேர்ந்தெடுக்கப்பட்ட படத்தில் QR குறியீடு கண்டறியப்படவில்லை.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // UPI Payment Confirmation Sheet
    var showUpiConfirmSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // CameraX Live Preview View
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, analyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = cam
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(Unit) {
                onDispose {
                    cameraExecutor.shutdown()
                }
            }

            // Scanning Overlay: Luxury Golden Frame with Animated Scan Line
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val scanLinePosition by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                // Golden Viewfinder Reticle (260dp x 260dp)
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.5.dp, GoldPrimary, RoundedCornerShape(24.dp))
                ) {
                    // Animated laser scan bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .padding(top = (250 * scanLinePosition).dp)
                            .background(GoldLight)
                    )
                }

                // Top instructions
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QR குறியீட்டைச் சட்டத்தில் பொருத்தவும்",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Align QR code inside the gold frame",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Bottom Controls: Flashlight & Gallery Pick
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Torch Button
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControl?.cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.5.dp, GoldPrimary, CircleShape)
                            .testTag("torch_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (isTorchOn) GoldPrimary else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Gallery Image Picker Button
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.5.dp, GoldPrimary, CircleShape)
                            .testTag("gallery_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick from Gallery",
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        } else {
            // Camera Permission Request & Gallery Fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "கேமரா அனுமதி தேவை",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "QR குறியீடுகளை நேரடியாக ஸ்கேன் செய்ய உங்கள் சாதனத்தின் கேமரா அனுமதி தேவைப்படுகிறது. இது உங்கள் சாதனத்தில் மட்டுமே பாதுகாப்பாகப் பயன்படுத்தப்படுகிறது.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("request_camera_perm_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("அனுமதியை இயக்கு (Enable Camera)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("gallery_pick_no_perm_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("படத்திலிருந்து ஸ்கேன் செய் (Gallery)", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Scanned Result Bottom Sheet
    if (scannedContent != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                scannedContent = null
                analyzer.resume()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (detectedType) {
                                QrType.UPI -> Icons.Default.CurrencyRupee
                                QrType.URL -> Icons.Default.OpenInBrowser
                                else -> Icons.Default.QrCodeScanner
                            },
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (detectedType) {
                                QrType.UPI -> "UPI பணம் செலுத்தும் QR"
                                QrType.URL -> "இணையதள இணைப்பு (URL)"
                                QrType.WIFI -> "வைஃபை நெட்வொர்க் (Wi-Fi)"
                                else -> "ஸ்கேன் செய்யப்பட்ட QR"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "தானாகக் கண்டறியப்பட்டது (Auto-detected)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        scannedContent = null
                        analyzer.resume()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content display card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (detectedType == QrType.UPI && detectedUpiDetails != null) {
                            Text(
                                text = "பெறுநர்: ${detectedUpiDetails!!.payeeName.ifBlank { "Merchant" }}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "VPA: ${detectedUpiDetails!!.vpa}",
                                fontSize = 13.sp,
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (detectedUpiDetails!!.amount.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "தொகை: ₹ ${detectedUpiDetails!!.amount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                            }
                        } else {
                            Text(
                                text = scannedContent ?: "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 6
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Smart Action Button
                if (detectedType == QrType.UPI && detectedUpiDetails != null) {
                    Button(
                        onClick = {
                            showUpiConfirmSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("scan_pay_upi_button")
                    ) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (detectedUpiDetails!!.amount.isNotBlank()) "₹ ${detectedUpiDetails!!.amount} செலுத்து (Pay Now)" else "UPI பணம் செலுத்து (Pay Now)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (detectedType == QrType.URL) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "இணைப்பைத் திறக்க இயலவில்லை", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("scan_open_browser_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("இணைப்பைத் திற (Open Website)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Copy & Share & Scan Again buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("CNV QR Text", scannedContent))
                            Toast.makeText(context, "உரை நகலெடுக்கப்பட்டது (Copied)", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("நகலெடு (Copy)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, scannedContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "QR உரையைப் பகிர்க"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("பகிர் (Share)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            scannedContent = null
                            analyzer.resume()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("மீண்டும்", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Confirmation Sheet for UPI if initiated from scanned result
    if (showUpiConfirmSheet && detectedUpiDetails != null) {
        UpiPaymentConfirmSheet(
            upiDetails = detectedUpiDetails!!,
            onDismiss = { showUpiConfirmSheet = false },
            onConfirmPay = { finalUri ->
                showUpiConfirmSheet = false
                val launched = UpiManager.launchUpiPayment(context, finalUri)
                if (!launched) {
                    Toast.makeText(context, "சாதனத்தில் UPI செயலி கண்டறியப்படவில்லை", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

private fun triggerHaptic(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(70)
            }
        }
    } catch (_: Exception) {}
}
