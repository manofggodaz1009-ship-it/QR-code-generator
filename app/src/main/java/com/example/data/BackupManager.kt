package com.example.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    fun exportToJson(context: Context, items: List<QrHistoryEntity>): Uri? {
        return try {
            val jsonArray = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("content", item.content)
                    put("title", item.title)
                    put("type", item.type)
                    put("isGenerated", item.isGenerated)
                    put("timestamp", item.timestamp)
                    put("isFavorite", item.isFavorite)
                    put("upiAmount", item.upiAmount ?: "")
                    put("upiVpa", item.upiVpa ?: "")
                    put("colorHex", item.colorHex ?: "")
                    put("hasCustomLogo", item.hasCustomLogo)
                }
                jsonArray.put(obj)
            }

            val root = JSONObject().apply {
                put("app", "CNV QR & UPI")
                put("version", "1.0")
                put("exportDate", System.currentTimeMillis())
                put("count", items.size)
                put("history", jsonArray)
            }

            val cachePath = File(context.cacheDir, "backups")
            if (!cachePath.exists()) cachePath.mkdirs()
            val backupFile = File(cachePath, "CNV_QR_Backup_${System.currentTimeMillis()}.json")
            FileOutputStream(backupFile).use { out ->
                out.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importFromJson(context: Context, uri: Uri): List<QrHistoryEntity> {
        val resultList = mutableListOf<QrHistoryEntity>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val jsonArray = root.optJSONArray("history") ?: return emptyList()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val entity = QrHistoryEntity(
                    id = 0, // autoGenerate new ID
                    content = obj.getString("content"),
                    title = obj.getString("title"),
                    type = obj.optString("type", "TEXT"),
                    isGenerated = obj.optBoolean("isGenerated", true),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    upiAmount = obj.optString("upiAmount").takeIf { it.isNotBlank() },
                    upiVpa = obj.optString("upiVpa").takeIf { it.isNotBlank() },
                    colorHex = obj.optString("colorHex").takeIf { it.isNotBlank() },
                    hasCustomLogo = obj.optBoolean("hasCustomLogo", false)
                )
                resultList.add(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }
}
