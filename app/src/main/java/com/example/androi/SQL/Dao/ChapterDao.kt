package com.example.androi.SQL.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androi.SQL.enity.ChapterEntity

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long?

    @Query("SELECT * FROM downloaded_chapters WHERE chapterId = :chapterId")
    suspend fun getChapterById(chapterId: Long): ChapterEntity?

    @Query("SELECT * FROM downloaded_chapters WHERE bookId = :bookId")
    suspend fun getChaptersByBook(bookId: Long): List<ChapterEntity>

    @Query("DELETE FROM downloaded_chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBook(bookId: Long): Int?
}