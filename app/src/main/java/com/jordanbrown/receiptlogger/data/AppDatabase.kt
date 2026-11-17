package com.jordanbrown.receiptlogger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** On-device SQLite database (via Room). Nothing is sent off the device. */
@Database(entities = [Receipt::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receipts.db"
                ).build().also { instance = it }
            }
    }
}
