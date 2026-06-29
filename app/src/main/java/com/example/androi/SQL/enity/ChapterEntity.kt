package com.example.androi.SQL.enity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_chapters")
data class ChapterEntity(
    @PrimaryKey val chapterId: Long,
    val bookId: Long,
    val title: String,
    val contentType: String,
    val textContent: String?,
    val localImagePaths: String?
)