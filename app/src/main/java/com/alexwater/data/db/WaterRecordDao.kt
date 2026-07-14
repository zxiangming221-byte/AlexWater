package com.alexwater.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterRecordDao {
    @Insert
    suspend fun insert(record: WaterRecordEntity): Long

    @Delete
    suspend fun delete(record: WaterRecordEntity)

    @Query("SELECT * FROM water_records WHERE date = :date ORDER BY timestamp DESC")
    fun getByDate(date: String): Flow<List<WaterRecordEntity>>

    @Query("SELECT * FROM water_records WHERE date BETWEEN :start AND :end ORDER BY date ASC, timestamp ASC")
    fun getByDateRange(start: String, end: String): Flow<List<WaterRecordEntity>>

    @Query("SELECT * FROM water_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<WaterRecordEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_records WHERE date = :date")
    fun getTotalByDate(date: String): Flow<Int>
}
