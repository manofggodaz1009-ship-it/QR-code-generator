package com.example.model

import android.net.Uri

enum class DotStyle(val titleTa: String, val titleEn: String) {
    SQUARE("சதுரம்", "Square"),
    ROUNDED("வளைந்த விளிம்பு", "Rounded"),
    CIRCLE("வட்டம்", "Circle")
}

enum class PresetLogo(val title: String, val iconResName: String) {
    NONE("லோகோ இல்லை", ""),
    RUPEE("₹ ரூபாய் / UPI", "ic_logo_rupee"),
    GPAY("GPay", "ic_logo_gpay"),
    PHONEPE("PhonePe", "ic_logo_phonepe"),
    PAYTM("Paytm", "ic_logo_paytm"),
    WHATSAPP("WhatsApp", "ic_logo_whatsapp"),
    WIFI("Wi-Fi", "ic_logo_wifi"),
    WEB("இணையம்", "ic_logo_web"),
    CALL("தொலைபேசி", "ic_logo_call"),
    HEART("அன்பு / இதயம்", "ic_logo_heart"),
    SHIELD("பாதுகாப்பு", "ic_logo_shield")
}

enum class LogoShape(val titleTa: String, val titleEn: String) {
    CIRCLE("வட்டம்", "Circle"),
    ROUNDED_RECT("வட்ட சதுரம்", "Rounded Box"),
    SQUARE("சதுரம்", "Square"),
    NONE("பின்னணி இல்லை", "Transparent")
}

data class QrConfig(
    val content: String = "",
    val foregroundColor: Int = 0xFFD4AF37.toInt(), // Luxury Gold
    val backgroundColor: Int = 0xFF0D0D11.toInt(), // Deep Obsidian Black
    val dotStyle: DotStyle = DotStyle.ROUNDED,
    val size: Int = 1024,
    // Logo customization
    val customLogoUri: Uri? = null,
    val presetLogo: PresetLogo = PresetLogo.NONE,
    val logoSizeRatio: Float = 0.20f, // 12% to 28% safe range
    val logoShape: LogoShape = LogoShape.CIRCLE,
    val logoBgColor: Int = 0xFFFFFFFF.toInt(), // Pure White badge default
    val logoPaddingFraction: Float = 0.12f,
    val logoOffsetYRatio: Float = 0f // 0 is exact center (best for QR scan)
)
