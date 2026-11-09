package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QrHistoryDatabase
import com.example.data.QrHistoryEntity
import com.example.generator.QrCodeGenerator
import com.example.model.DotStyle
import com.example.model.LogoShape
import com.example.model.PresetLogo
import com.example.model.QrConfig
import com.example.model.QrTemplate
import com.example.model.QrTemplateCatalog
import com.example.model.QrType
import com.example.ui.components.LogoEditorSection
import com.example.ui.components.QrStyleSelector
import com.example.ui.components.UpiPaymentConfirmSheet
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.upi.UpiDetails
import com.example.upi.UpiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateQrScreen(
    initialTemplate: QrTemplate? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { QrHistoryDatabase.getDatabase(context) }

    val currentTemplate = remember {
        initialTemplate ?: QrTemplateCatalog.templates.first { it.id == "upi_general" }
    }

    // Input States
    var upiVpa by remember { mutableStateOf(if (currentTemplate.type == QrType.UPI) "merchant@upi" else "") }
    var upiPayeeName by remember { mutableStateOf(if (currentTemplate.type == QrType.UPI) "Store Name" else "") }
    var upiAmount by remember { mutableStateOf("") }
    var upiNote by remember { mutableStateOf("") }

    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiType by remember { mutableStateOf("WPA") }

    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }

    var genericText by remember { mutableStateOf(if (currentTemplate.type != QrType.UPI) currentTemplate.defaultPrefix else "") }

    // QrConfig State
    var qrConfig by remember {
        mutableStateOf(
            QrConfig(
                content = "",
                foregroundColor = 0xFFD4AF37.toInt(), // Luxury Gold
                backgroundColor = 0xFF0C0C0F.toInt(), // Deep Obsidian
                dotStyle = DotStyle.ROUNDED,
                presetLogo = if (currentTemplate.type == QrType.UPI) PresetLogo.RUPEE else PresetLogo.NONE,
                logoSizeRatio = 0.20f,
                logoShape = LogoShape.CIRCLE,
                logoBgColor = 0xFFFFFFFF.toInt()
            )
        )
    }

    // Generated QR State
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var verifiedScannableText by remember { mutableStateOf<String?>(null) }
    var isScannableStatus by remember { mutableStateOf<Boolean?>(null) }

    // UPI Payment confirmation sheet state
    var showUpiConfirmSheet by remember { mutableStateOf(false) }
    var activeUpiDetails by remember { mutableStateOf<UpiDetails?>(null) }

    // Build Payload string
    val currentPayload = remember(
        currentTemplate.type,
        upiVpa, upiPayeeName, upiAmount, upiNote,
        wifiSsid, wifiPassword, wifiType,
        contactName, contactPhone, contactEmail, contactOrg,
        genericText
    ) {
        when (currentTemplate.type) {
            QrType.UPI -> {
                if (upiVpa.isNotBlank()) {
                    UpiManager.buildUpiUri(
                        vpa = upiVpa.trim(),
                        payeeName = upiPayeeName.trim(),
                        amount = upiAmount.trim(),
                        note = upiNote.trim()
                    )
                } else ""
            }
            QrType.WIFI -> {
                if (wifiSsid.isNotBlank()) {
                    "WIFI:S:${wifiSsid.trim()};T:$wifiType;P:${wifiPassword.trim()};;"
                } else ""
            }
            QrType.CONTACT -> {
                if (contactName.isNotBlank() || contactPhone.isNotBlank()) {
                    "BEGIN:VCARD\nVERSION:3.0\nFN:${contactName.trim()}\nTEL:${contactPhone.trim()}\nEMAIL:${contactEmail.trim()}\nORG:${contactOrg.trim()}\nEND:VCARD"
                } else ""
            }
            QrType.WHATSAPP -> {
                if (genericText.isNotBlank()) {
                    val clean = genericText.replace("+", "").replace(" ", "").trim()
                    "https://wa.me/$clean"
                } else ""
            }
            QrType.PHONE -> {
                if (genericText.isNotBlank()) "tel:${genericText.trim()}" else ""
            }
            QrType.SMS -> {
                if (genericText.isNotBlank()) "sms:${genericText.trim()}" else ""
            }
            QrType.EMAIL -> {
                if (genericText.isNotBlank()) "mailto:${genericText.trim()}" else ""
            }
            QrType.LOCATION -> {
                if (genericText.isNotBlank()) {
                    if (genericText.contains("http")) genericText.trim() else "geo:${genericText.trim()}"
                } else ""
            }
            else -> {
                genericText.trim()
            }
        }
    }

    // Auto generate QR whenever payload or styling changes
    LaunchedEffect(currentPayload, qrConfig) {
        if (currentPayload.isNotBlank()) {
            isGenerating = true
            val updatedConfig = qrConfig.copy(content = currentPayload)
            withContext(Dispatchers.Default) {
                val bmp = QrCodeGenerator.generateQrBitmap(context, updatedConfig)
                val testResult = if (bmp != null) QrCodeGenerator.verifyScannability(bmp) else null
                withContext(Dispatchers.Main) {
                    generatedBitmap = bmp
                    verifiedScannableText = testResult
                    isScannableStatus = (testResult != null)
                    isGenerating = false
                }
            }
        } else {
            generatedBitmap = null
            isScannableStatus = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Title & Description
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTemplate.titleTa,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentTemplate.titleEn} • ${currentTemplate.categoryTa}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dynamic Input Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "விவரங்களை உள்ளிடுக (Enter Details):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentTemplate.type) {
                        QrType.UPI -> {
                            OutlinedTextField(
                                value = upiVpa,
                                onValueChange = { upiVpa = it },
                                label = { Text("UPI ID / VPA * (எ.கா: merchant@oksbi)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_upi_vpa_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = upiPayeeName,
                                onValueChange = { upiPayeeName = it },
                                label = { Text("பெறுநர் பெயர் / Payee Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_upi_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = upiAmount,
                                    onValueChange = { upiAmount = it },
                                    label = { Text("தொகை ₹ (Amount - Optional)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("create_upi_amount_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = upiNote,
                                    onValueChange = { upiNote = it },
                                    label = { Text("குறிப்பு (Note)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("create_upi_note_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                        QrType.WIFI -> {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                label = { Text("வைஃபை பெயர் / Network SSID *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_wifi_ssid_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = { wifiPassword = it },
                                label = { Text("கடவுச்சொல் / Password") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_wifi_pass_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        QrType.CONTACT -> {
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = { contactName = it },
                                label = { Text("முழு பெயர் / Full Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_contact_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("தொலைபேசி எண் / Phone *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_contact_phone_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                label = { Text("மின்னஞ்சல் / Email") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_contact_email_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        else -> {
                            OutlinedTextField(
                                value = genericText,
                                onValueChange = { genericText = it },
                                label = { Text("உள்ளடக்கம் / Content *") },
                                placeholder = { Text(currentTemplate.defaultPlaceholder.ifBlank { "உள்ளடக்கம் உள்ளிடுக..." }) },
                                minLines = 2,
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_generic_text_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Live QR Code Preview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "நேரடி முன்னோட்டம் (Live QR Preview)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(qrConfig.backgroundColor))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(36.dp))
                        } else if (generatedBitmap != null) {
                            Image(
                                bitmap = generatedBitmap!!.asImageBitmap(),
                                contentDescription = "Generated QR Code",
                                modifier = Modifier
                                    .size(220.dp)
                                    .testTag("qr_preview_image")
                            )
                        } else {
                            Text(
                                text = "தகவல்களை உள்ளிடவும்...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scannability test result
                    if (isScannableStatus != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isScannableStatus == true) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isScannableStatus == true) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isScannableStatus == true)
                                    "✓ தானியங்கி ஸ்கேன் சரிபார்க்கப்பட்டது: 100% படிக்கக்கூடியது"
                                else
                                    "⚠ லோகோவின் அளவைக் குறைக்கவும் (ஸ்கேன் சரிபார்க்க இயலவில்லை)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isScannableStatus == true) Color(0xFF4CAF50) else Color(0xFFEF5350)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Custom Logo Embedding Editor Section (Upload, Size Slider, Shape Badge)
        item {
            LogoEditorSection(
                config = qrConfig,
                onConfigChange = { qrConfig = it },
                isScannable = isScannableStatus
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // QR Colors and Dot Style Selector
        item {
            QrStyleSelector(
                config = qrConfig,
                onConfigChange = { qrConfig = it }
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Primary Action Buttons
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // If UPI, offer immediate UPI Pay Launcher
                if (currentTemplate.type == QrType.UPI && upiVpa.isNotBlank()) {
                    Button(
                        onClick = {
                            activeUpiDetails = UpiManager.parseUpiUri(currentPayload)
                            showUpiConfirmSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_via_upi_action_button")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "இப்போது UPI மூலம் செலுத்து (Pay Now)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Save to Gallery
                    Button(
                        onClick = {
                            if (generatedBitmap != null) {
                                val uri = QrCodeGenerator.saveQrToGallery(context, generatedBitmap!!, currentTemplate.titleTa)
                                if (uri != null) {
                                    Toast.makeText(context, "QR படம் சேமிக்கப்பட்டது! (Saved to Pictures/CNV_QR)", Toast.LENGTH_SHORT).show()
                                    // Save to Room DB
                                    scope.launch {
                                        db.qrHistoryDao().insert(
                                            QrHistoryEntity(
                                                content = currentPayload,
                                                title = currentTemplate.titleTa,
                                                type = currentTemplate.type.name,
                                                isGenerated = true,
                                                upiAmount = upiAmount.takeIf { it.isNotBlank() },
                                                upiVpa = upiVpa.takeIf { it.isNotBlank() },
                                                hasCustomLogo = qrConfig.customLogoUri != null || qrConfig.presetLogo != PresetLogo.NONE
                                            )
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "சேமிப்பதில் பிழை ஏற்பட்டது", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = generatedBitmap != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_qr_gallery_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("சேமி (Save PNG)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Share QR
                    OutlinedButton(
                        onClick = {
                            if (generatedBitmap != null) {
                                val shareUri = QrCodeGenerator.getShareableUri(context, generatedBitmap!!)
                                if (shareUri != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        putExtra(Intent.EXTRA_TEXT, "CNV QR & UPI மூலம் உருவாக்கப்பட்டது:\n$currentPayload")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "QR குறியீட்டைப் பகிர்க"))
                                }
                            }
                        },
                        enabled = generatedBitmap != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_qr_image_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("பகிர் (Share)", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // UPI Payment Confirmation Sheet
    if (showUpiConfirmSheet && activeUpiDetails != null) {
        UpiPaymentConfirmSheet(
            upiDetails = activeUpiDetails!!,
            onDismiss = { showUpiConfirmSheet = false },
            onConfirmPay = { finalUri ->
                showUpiConfirmSheet = false
                val launched = UpiManager.launchUpiPayment(context, finalUri)
                if (!launched) {
                    Toast.makeText(context, "சாதனத்தில் UPI செயலிகள் எதுவும் கண்டறியப்படவில்லை (No UPI app found)", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
