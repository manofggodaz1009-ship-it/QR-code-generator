package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cnv_qr_prefs", Context.MODE_PRIVATE)

    var isTermsAccepted: Boolean
        get() = prefs.getBoolean("key_terms_accepted", false)
        set(value) = prefs.edit().putBoolean("key_terms_accepted", value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean("key_dark_mode", true)
        set(value) = prefs.edit().putBoolean("key_dark_mode", value).apply()

    var languageMode: String
        get() = prefs.getString("key_lang_mode", "BOTH") ?: "BOTH"
        set(value) = prefs.edit().putString("key_lang_mode", value).apply()
}
