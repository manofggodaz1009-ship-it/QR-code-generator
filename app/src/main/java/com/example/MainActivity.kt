package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.model.QrTemplate
import com.example.model.QrTemplateCatalog
import com.example.ui.components.GoldTopBar
import com.example.ui.components.TermsConsentDialog
import com.example.ui.components.UpiPaymentConfirmSheet
import com.example.ui.screens.CreateQrScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScanQrScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CnvQrTheme
import com.example.ui.theme.GoldPrimary
import com.example.upi.UpiDetails
import com.example.upi.UpiManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)

        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.isDarkMode) }
            var languageMode by remember { mutableStateOf(prefs.languageMode) }
            var isTermsAccepted by remember { mutableStateOf(prefs.isTermsAccepted) }

            // Navigation state: 0 = Home, 1 = Create, 2 = Scan, 3 = History, 4 = Settings
            var currentTab by remember { mutableIntStateOf(0) }
            var selectedTemplate by remember { mutableStateOf<QrTemplate?>(null) }

            // Global Quick UPI Payment Sheet
            var showUpiConfirmSheet by remember { mutableStateOf(false) }
            var quickUpiDetails by remember { mutableStateOf<UpiDetails?>(null) }

            CnvQrTheme(darkTheme = isDarkMode) {
                // If on secondary tab, back press navigates to Home
                BackHandler(enabled = currentTab != 0) {
                    currentTab = 0
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        GoldTopBar(
                            titleTa = "CNV QR & UPI",
                            subtitleEn = when (currentTab) {
                                0 -> "முகப்பு • Home"
                                1 -> "உருவாக்கு • Generator & Logo"
                                2 -> "ஸ்கேன் • Live Scanner"
                                3 -> "வரலாறு • History & Backup"
                                4 -> "அமைப்புகள் • Settings & Policies"
                                else -> "CNV Ministries"
                            },
                            onLangClick = {
                                val nextLang = when (languageMode) {
                                    "BOTH" -> "TA"
                                    "TA" -> "EN"
                                    else -> "BOTH"
                                }
                                languageMode = nextLang
                                prefs.languageMode = nextLang
                                Toast.makeText(
                                    this,
                                    when (nextLang) {
                                        "TA" -> "மொழி: தமிழ் மட்டும்"
                                        "EN" -> "Language: English Only"
                                        else -> "மொழி: தமிழ் + English (இருமொழி)"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            val items = listOf(
                                NavigationItem("முகப்பு", "Home", Icons.Default.Home, "nav_home"),
                                NavigationItem("உருவாக்கு", "Create", Icons.Default.AddCircle, "nav_create"),
                                NavigationItem("ஸ்கேன்", "Scan", Icons.Default.QrCodeScanner, "nav_scan"),
                                NavigationItem("வரலாறு", "History", Icons.Default.History, "nav_history"),
                                NavigationItem("அமைப்புகள்", "Settings", Icons.Default.Settings, "nav_settings")
                            )

                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = currentTab == index,
                                    onClick = {
                                        if (index == 1 && currentTab != 1) {
                                            // Reset to general upi template if clicking tab directly
                                            if (selectedTemplate == null) {
                                                selectedTemplate = QrTemplateCatalog.templates.first { it.id == "upi_general" }
                                            }
                                        }
                                        currentTab = index
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.titleEn,
                                            tint = if (currentTab == index) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (languageMode == "EN") item.titleEn else item.titleTa,
                                            fontSize = 10.sp,
                                            fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentTab == index) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = GoldPrimary
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                onSelectTemplate = { template ->
                                    selectedTemplate = template
                                    currentTab = 1
                                },
                                onNavigateToCreate = {
                                    currentTab = 1
                                },
                                onNavigateToScan = {
                                    currentTab = 2
                                },
                                onNavigateToHistory = {
                                    currentTab = 3
                                },
                                onQuickUpiPay = { vpa, amount ->
                                    quickUpiDetails = UpiDetails(
                                        vpa = vpa,
                                        amount = amount,
                                        rawUri = UpiManager.buildUpiUri(vpa = vpa, amount = amount)
                                    )
                                    showUpiConfirmSheet = true
                                }
                            )
                            1 -> CreateQrScreen(
                                initialTemplate = selectedTemplate,
                                onNavigateBack = { currentTab = 0 }
                            )
                            2 -> ScanQrScreen()
                            3 -> HistoryScreen()
                            4 -> SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeToggle = { enabled ->
                                    isDarkMode = enabled
                                    prefs.isDarkMode = enabled
                                },
                                languageMode = languageMode,
                                onLanguageChange = { newMode ->
                                    languageMode = newMode
                                    prefs.languageMode = newMode
                                }
                            )
                        }
                    }
                }

                // First-time Terms Consent Dialog
                if (!isTermsAccepted) {
                    TermsConsentDialog(
                        onAccept = {
                            isTermsAccepted = true
                            prefs.isTermsAccepted = true
                        }
                    )
                }

                // Global UPI Payment Confirmation Sheet
                if (showUpiConfirmSheet && quickUpiDetails != null) {
                    UpiPaymentConfirmSheet(
                        upiDetails = quickUpiDetails!!,
                        onDismiss = { showUpiConfirmSheet = false },
                        onConfirmPay = { finalUri ->
                            showUpiConfirmSheet = false
                            val launched = UpiManager.launchUpiPayment(this, finalUri)
                            if (!launched) {
                                Toast.makeText(
                                    this,
                                    "சாதனத்தில் UPI செயலி கண்டறியப்படவில்லை. தயவுசெய்து Google Pay அல்லது PhonePe நிறுவவும்.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val titleTa: String,
    val titleEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
