package com.example.upi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.net.URLDecoder
import java.net.URLEncoder

data class UpiDetails(
    val vpa: String, // pa: Virtual Payment Address (e.g. user@okhdfcbank)
    val payeeName: String = "", // pn: Payee Name
    val amount: String = "", // am: Amount (e.g. 500.00)
    val note: String = "", // tn: Transaction note
    val currency: String = "INR", // cu: Default INR
    val merchantCode: String = "", // mc: Merchant category code
    val rawUri: String = ""
)

object UpiManager {

    /**
     * Checks if string is a valid UPI URI
     */
    fun isUpiUri(text: String): Boolean {
        return text.trim().startsWith("upi://pay", ignoreCase = true)
    }

    /**
     * Parses a UPI URI string into UpiDetails
     */
    fun parseUpiUri(uriString: String): UpiDetails? {
        if (!isUpiUri(uriString)) return null
        return try {
            val uri = Uri.parse(uriString)
            val pa = uri.getQueryParameter("pa") ?: ""
            val pn = uri.getQueryParameter("pn") ?: ""
            val am = uri.getQueryParameter("am") ?: ""
            val tn = uri.getQueryParameter("tn") ?: ""
            val cu = uri.getQueryParameter("cu") ?: "INR"
            val mc = uri.getQueryParameter("mc") ?: ""
            UpiDetails(
                vpa = URLDecoder.decode(pa, "UTF-8"),
                payeeName = URLDecoder.decode(pn, "UTF-8"),
                amount = URLDecoder.decode(am, "UTF-8"),
                note = URLDecoder.decode(tn, "UTF-8"),
                currency = cu,
                merchantCode = mc,
                rawUri = uriString
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Builds a standard UPI URI
     */
    fun buildUpiUri(
        vpa: String,
        payeeName: String = "",
        amount: String = "",
        note: String = "",
        currency: String = "INR"
    ): String {
        val cleanVpa = vpa.trim()
        val builder = StringBuilder("upi://pay?pa=").append(URLEncoder.encode(cleanVpa, "UTF-8"))
        if (payeeName.isNotBlank()) {
            builder.append("&pn=").append(URLEncoder.encode(payeeName.trim(), "UTF-8"))
        }
        if (amount.isNotBlank()) {
            // Remove comma if any
            val cleanAmount = amount.trim().replace(",", "")
            builder.append("&am=").append(cleanAmount)
        }
        builder.append("&cu=").append(currency)
        if (note.isNotBlank()) {
            builder.append("&tn=").append(URLEncoder.encode(note.trim(), "UTF-8"))
        }
        return builder.toString()
    }

    /**
     * Launches UPI Intent to perform payment.
     * Returns true if an app could handle the intent, false otherwise.
     */
    fun launchUpiPayment(context: Context, upiUri: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(upiUri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Returns list of installed UPI apps names (if query permission matches)
     */
    fun getInstalledUpiApps(context: Context): List<String> {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("upi://pay")
            }
            val pm = context.packageManager
            val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            activities.map { it.loadLabel(pm).toString() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
