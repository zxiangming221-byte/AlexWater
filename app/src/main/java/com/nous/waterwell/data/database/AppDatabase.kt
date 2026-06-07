package com.nous.waterwell.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nous.waterwell.data.model.DrinkRecord

@Database(entities = [DrinkRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "waterwell.db")
                    .build()
            } catch (e: Exception) {
                // Database corrupted — delete and recreate
                context.applicationContext.deleteDatabase("waterwell.db")
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "waterwell.db")
                    .build()
            }
        }
    }
}
