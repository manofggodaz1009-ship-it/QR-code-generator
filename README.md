# CNV QR & UPI — முழுமையான Android Project

**CNV QR & UPI** (`com.cnvministries.qrupi`) என்பது Kotlin மற்றும் Jetpack Compose கொண்டு உருவாக்கப்பட்ட அதிநவீன Native Android செயலி ஆகும்.

---

## 🌟 முக்கிய அம்சங்கள் (Key Features)

1. **விருப்ப லோகோ சேர்த்தல் (Custom Logo Embedding)**:
   - கேலரி புகைப்படத்தைப் பதிவேற்றி QR-க்குள் லோகோவாக இணைக்கும் வசதி.
   - முன்வடிவமைக்கப்பட்ட சின்னங்கள் (₹ ரூபாய், Google Pay, PhonePe, Paytm, WhatsApp, Wi-Fi, Call, Heart).
   - லோகோ அளவு (12% - 28%) மற்றும் வடிவக் கட்டுப்பாடு (வட்டம், சதுரம், வட்ட சதுரம்).
   - ZXing `ErrorCorrectionLevel.H` மூலம் லோகோ இருந்தாலும் 100% படிக்கக்கூடிய உத்தரவாதம்.
   - நிகழ்நேர ஸ்கேன் சரிபார்ப்பு (Scannability Validator).

2. **100+ QR வடிவங்கள் (Templates Catalog)**:
   - பணம் & UPI (22 வடிவங்கள்)
   - சமூக வலைத்தளங்கள் & சேட் (25 வடிவங்கள்)
   - தொடர்புகள் & விசிட்டிங் கார்டு (15 வடிவங்கள்)
   - இணையம் & Wi-Fi (10 வடிவங்கள்)
   - இருப்பிடம் & வரைபடம் (10 வடிவங்கள்)
   - இணையம், செயலிகள் & குறிப்புகள் (28 வடிவங்கள்)

3. **UPI கட்டண முறைமை (UPI Pay Integration)**:
   - `upi://pay` இணைப்புகள் & QR குறியீடுகள் உருவாக்கம்.
   - பணம் செலுத்தும் முன் பயனர் பெயர், தொகை, குறிப்பு ஆகியவற்றைச் சரிபார்க்கும் உறுதிப்படுத்தல் திரை (Confirmation Sheet).
   - Google Pay, PhonePe, Paytm செயலிகளுக்கு நேரடி Intent இணைப்பு.

4. **கேமரா & கேலரி QR ஸ்கேனர் (High-Speed Scanner)**:
   - CameraX நேரடி கேமரா ஸ்கேனிங்.
   - ஃப்ளாஷ்லைட் (Torch) ஆன்/ஆஃப்.
   - கேலரி படத்திலிருந்து QR கோட் ஸ்கேன்.
   - ஸ்மார்ட் செயல்பாடுகள்: நேரடி UPI பணம் செலுத்துதல், URL திறத்தல், நகலெடுத்தல், பகிர்தல்.

5. **வரலாறு & JSON காப்புப்பிரதி (History & JSON Backup)**:
   - உள்ளக SQLite / Room Database களஞ்சியம்.
   - JSON Backup Export மற்றும் Import வசதி.

6. **வடிவமைப்பு & தனியுரிமை**:
   - ஆடம்பர Premium Black & Gold தீம்.
   - தமிழ் & ஆங்கில இருமொழி ஆதரவு.
   - 100% ஆன்-டிவைஸ் பாதுகாப்பு (தகவல்கள் எங்கும் அனுப்பப்படுவதில்லை).

---

## 🚀 Android Studio-வில் திறக்கும் முறை (How to Open in Android Studio)

1. **CNV_QR_UPI_Project.zip** கோப்பைப் பிரித்தெடுக்கவும் (Extract ZIP).
2. **Android Studio**-வை திறக்கவும்.
3. **Open an Existing Project** என்பதைத் தேர்ந்தெடுத்து பிரித்தெடுத்த கோப்புறையைத் தேர்ந்தெடுக்கவும்.
4. Gradle Sync தானாக முடிவடையும் வரை காத்திருக்கவும்.
5. **Run 'app' (Shift + F10)** கிளிக் செய்து எமுலேட்டர் அல்லது உங்கள் மொபைலில் இயக்கவும்.

---

## 🛠️ தொழில்நுட்ப விவரங்கள் (Tech Stack)

- **Language:** Kotlin 2.2+
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM / Clean Single Activity
- **Database:** Android Room 2.7+
- **Camera:** CameraX 1.5+
- **Barcode Engine:** ZXing Core 3.5.3
- **Image Loading:** Coil Compose
- **Min SDK:** 24 (Android 7.0+)
- **Target SDK:** 36 (Android 15+)
- **Application ID:** `com.cnvministries.qrupi`

Developed for **CNV Ministries**.
