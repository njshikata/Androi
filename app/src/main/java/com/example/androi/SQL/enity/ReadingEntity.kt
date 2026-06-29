package com.example.androi.SQL.enity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_history")
data class ReadingEntity(
    @PrimaryKey val bookId: Long,
    val lastChapterId: Long,
    val lastChapterTitle: String,
    val lastScrollPosition: Int = 0, // Lưu vị trí cuộn để đọc tiếp chính xác
    val timestamp: Long = System.currentTimeMillis()
)