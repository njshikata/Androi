package com.example.androi.SQL.enity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_books")
data class BookEntity(
    @PrimaryKey val bookId: Long,
    val title: String,
    val coverUrl: String,
    val author: String,
    val totalChapters: Int = 0
)
