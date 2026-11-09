package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DotStyle
import com.example.model.QrConfig
import com.example.ui.theme.GoldPrimary

@Composable
fun QrStyleSelector(
    config: QrConfig,
    onConfigChange: (QrConfig) -> Unit
) {
    val fgColors = listOf(
        0xFFD4AF37.toInt() to "தங்கம் (Gold)",
        0xFFFFC107.toInt() to "ஆம்பர் (Amber)",
        0xFF000000.toInt() to "கருப்பு (Black)",
        0xFFFFFFFF.toInt() to "வெள்ளை (White)",
        0xFF2196F3.toInt() to "நீலம் (Blue)",
        0xFF4CAF50.toInt() to "பச்சை (Green)"
    )

    val bgColors = listOf(
        0xFF0C0C0F.toInt() to "கரும் கருப்பு (Pitch Black)",
        0xFF141419.toInt() to "டார்க் (Obsidian)",
        0xFFFFFFFF.toInt() to "வெள்ளை (Pure White)",
        0xFFF7F5EE.toInt() to "கிரீம் (Cream)"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "QR நிறம் & வடிவமைப்பு (Color & Style)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Foreground color
            Text(
                text = "QR குறியீடு நிறம் (Foreground):",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fgColors.forEach { (colorVal, _) ->
                    val isSelected = config.foregroundColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                2.dp,
                                if (isSelected) GoldPrimary else Color.Gray.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clickable { onConfigChange(config.copy(foregroundColor = colorVal)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorVal == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Background color
            Text(
                text = "பின்னணி நிறம் (Background):",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                bgColors.forEach { (colorVal, _) ->
                    val isSelected = config.backgroundColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                2.dp,
                                if (isSelected) GoldPrimary else Color.Gray.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clickable { onConfigChange(config.copy(backgroundColor = colorVal)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorVal == 0xFFFFFFFF.toInt() || colorVal == 0xFFF7F5EE.toInt()) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dot Style
            Text(
                text = "புள்ளி வடிவம் (Dot Style):",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotStyle.values().forEach { style ->
                    val isSelected = config.dotStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(dotStyle = style)) },
                        label = {
                            Text(
                                text = "${style.titleTa} (${style.titleEn})",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
