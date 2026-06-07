package com.nous.waterwell.data.database

import androidx.room.*
import com.nous.waterwell.data.model.DrinkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drink_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DrinkRecord>>

    // ── Today queries (no param — use SQLite date('now','localtime') to always get current day) ──
    @Query("SELECT * FROM drink_records WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY timestamp DESC")
    fun getTodayRecords(): Flow<List<DrinkRecord>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_records WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTodayTotal(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_records WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun getTodayTotalNow(): Int

    @Query("SELECT * FROM drink_records WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestToday(): DrinkRecord?

    @Query("DELETE FROM drink_records WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun deleteToday()

    // ── General CRUD ──────────────────────────────────────────────
    @Insert
    suspend fun insert(record: DrinkRecord): Long

    @Delete
    suspend fun delete(record: DrinkRecord)

    @Query("DELETE FROM drink_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
