package com.example.androi.SQL

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androi.SQL.Dao.BookDao
import com.example.androi.SQL.Dao.ChapterDao
import com.example.androi.SQL.Dao.ReadingDao
import com.example.androi.SQL.Dao.NotificationDao // 👉 Thêm import Dao
import com.example.androi.SQL.enity.BookEntity
import com.example.androi.SQL.enity.ChapterEntity
import com.example.androi.SQL.enity.ReadingEntity
import com.example.androi.SQL.enity.NotificationEntity // 👉 Thêm import Entity

// 👉 Thêm NotificationEntity::class vào entities và tăng version lên 4
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingDao(): ReadingDao

    // 👉 Khai báo thêm DAO cho Notification
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book"
                )
                    .fallbackToDestructiveMigration() // Sẽ xoá trắng data cũ khi update version 3 -> 4
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}