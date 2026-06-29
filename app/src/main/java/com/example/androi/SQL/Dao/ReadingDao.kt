package com.example.androi.SQL.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androi.SQL.enity.ReadingEntity

@Dao
interface ReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHistory(history: ReadingEntity): Long?

    @Query("SELECT * FROM reading_history WHERE bookId = :bookId")
    suspend fun getHistoryByBook(bookId: Long): ReadingEntity?

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    suspend fun getRecentHistory(): List<ReadingEntity>
}