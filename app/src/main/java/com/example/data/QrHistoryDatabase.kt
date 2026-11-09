package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QrHistoryEntity::class], version = 1, exportSchema = false)
abstract class QrHistoryDatabase : RoomDatabase() {
    abstract fun qrHistoryDao(): QrHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: QrHistoryDatabase? = null

        fun getDatabase(context: Context): QrHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QrHistoryDatabase::class.java,
                    "cnv_qr_upi_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
