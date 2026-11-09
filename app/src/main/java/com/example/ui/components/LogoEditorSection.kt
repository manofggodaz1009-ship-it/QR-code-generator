package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LogoShape
import com.example.model.PresetLogo
import com.example.model.QrConfig
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun LogoEditorSection(
    config: QrConfig,
    onConfigChange: (QrConfig) -> Unit,
    isScannable: Boolean? = true
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onConfigChange(
                config.copy(
                    customLogoUri = uri,
                    presetLogo = PresetLogo.NONE
                )
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Logo",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QR குறியீட்டில் லோகோ சேர்த்தல்",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Embed Custom Logo & Brand Mark",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Scannability Status Badge
                if (isScannable != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isScannable) Color(0xFF1B5E20).copy(alpha = 0.2f)
                                else Color(0xFFB71C1C).copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isScannable) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isScannable) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isScannable) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isScannable) "ஸ்கேன் சரியானது" else "சரிபார்க்கவும்",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isScannable) Color(0xFF4CAF50) else Color(0xFFEF5350)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Upload Custom Logo Button or Current Preview
            Text(
                text = "1. விருப்பமான லோகோ தேர்வு (Select / Upload Logo):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_custom_logo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload",
                        tint = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (config.customLogoUri != null) "லோகோவை மாற்று" else "படத்தை பதிவேற்று (Upload)",
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )
                }

                if (config.customLogoUri != null || config.presetLogo != PresetLogo.NONE) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    customLogoUri = null,
                                    presetLogo = PresetLogo.NONE
                                )
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF331A1A), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .testTag("remove_logo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove Logo",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Show selected custom logo preview if present
            if (config.customLogoUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = config.customLogoUri,
                        contentDescription = "Selected Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "விருப்ப லோகோ இணைக்கப்பட்டது",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Custom logo active (Error correction Level H)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Logos Catalog
            Text(
                text = "அல்லது முன்வடிவமைக்கப்பட்ட சின்னங்கள் (Preset Logos):",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PresetLogo.values()) { preset ->
                    val isSelected = config.presetLogo == preset && config.customLogoUri == null
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    presetLogo = preset,
                                    customLogoUri = null
                                )
                            )
                        },
                        label = {
                            Text(
                                text = preset.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            val hasActiveLogo = config.customLogoUri != null || config.presetLogo != PresetLogo.NONE
            if (hasActiveLogo) {
                Spacer(modifier = Modifier.height(16.dp))

                // Step 2: Logo Size Adjustment Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2. லோகோ அளவு (Size): ${(config.logoSizeRatio * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "(பாதுகாப்பான வரம்பு 12% - 28%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = config.logoSizeRatio,
                    onValueChange = { onConfigChange(config.copy(logoSizeRatio = it)) },
                    valueRange = 0.12f..0.28f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("logo_size_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step 3: Logo Shape Picker
                Text(
                    text = "3. லோகோ வடிவம் & பின்னணி (Shape & Badge):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogoShape.values().forEach { shape ->
                        val isShapeSelected = config.logoShape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isShapeSelected) GoldPrimary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onConfigChange(config.copy(logoShape = shape)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                when (shape) {
                                    LogoShape.CIRCLE -> Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    LogoShape.ROUNDED_RECT -> Icon(Icons.Default.CropSquare, contentDescription = null, tint = if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    LogoShape.SQUARE -> Icon(Icons.Default.CropSquare, contentDescription = null, tint = if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    LogoShape.NONE -> Icon(Icons.Default.Clear, contentDescription = null, tint = if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = shape.titleTa,
                                    fontSize = 10.sp,
                                    fontWeight = if (isShapeSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isShapeSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Badge Background Color Options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "பேட்ஜ் பின்னணி நிறம்:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    val badgeColors = listOf(
                        0xFFFFFFFF.toInt() to "வெள்ளை (White)",
                        0xFF000000.toInt() to "கருப்பு (Black)",
                        0xFFD4AF37.toInt() to "தங்கம் (Gold)",
                        0xFFFFF8E7.toInt() to "கிரீம் (Cream)"
                    )

                    badgeColors.forEach { (colorVal, _) ->
                        val isColorSelected = config.logoBgColor == colorVal
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    2.dp,
                                    if (isColorSelected) GoldPrimary else Color.Gray.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .clickable { onConfigChange(config.copy(logoBgColor = colorVal)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isColorSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (colorVal == 0xFFFFFFFF.toInt() || colorVal == 0xFFFFF8E7.toInt()) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
