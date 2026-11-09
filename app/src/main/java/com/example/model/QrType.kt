package com.example.model

enum class QrType(val titleTa: String, val titleEn: String) {
    UPI("UPI பணப்பரிவர்த்தனை", "UPI Payment"),
    TEXT("உரை (Text)", "Plain Text"),
    URL("இணைய முகவரி (URL)", "Website URL"),
    WIFI("வைஃபை (Wi-Fi)", "Wi-Fi Network"),
    CONTACT("தொடர்பு அட்டை (vCard)", "Contact Card"),
    EMAIL("மின்னஞ்சல் (Email)", "Email Address"),
    PHONE("தொலைபேசி (Phone)", "Phone Call"),
    SMS("குறுஞ்செய்தி (SMS)", "SMS Message"),
    LOCATION("இருப்பிடம் (Location)", "Geo Location"),
    WHATSAPP("வாட்ஸ்அப் (WhatsApp)", "WhatsApp Message"),
    TELEGRAM("டெலிகிராம் (Telegram)", "Telegram Link"),
    INSTAGRAM("இன்ஸ்டாகிராம் (Instagram)", "Instagram Profile"),
    YOUTUBE("யூடியூப் (YouTube)", "YouTube Channel/Video"),
    FACEBOOK("ஃபேஸ்புக் (Facebook)", "Facebook Page"),
    TWITTER("ட்விட்டர் / X", "Twitter / X Profile"),
    LINKEDIN("லிங்க்ட்இன் (LinkedIn)", "LinkedIn Profile"),
    GITHUB("கிட்ஹப் (GitHub)", "GitHub Repository"),
    EVENT("நிகழ்வு (Calendar)", "Calendar Event"),
    CRYPTO("கிரிப்டோ (Crypto)", "Crypto Wallet"),
    CUSTOM("தனிப்பயன் QR", "Custom Template")
}

data class QrTemplate(
    val id: String,
    val type: QrType,
    val categoryTa: String,
    val categoryEn: String,
    val titleTa: String,
    val titleEn: String,
    val descriptionTa: String,
    val descriptionEn: String,
    val defaultIconName: String,
    val defaultPrefix: String = "",
    val defaultPlaceholder: String = ""
)
