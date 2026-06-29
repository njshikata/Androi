package com.example.androi.SQL

import android.content.Context
import com.example.androi.SQL.enity.ChapterEntity
import com.example.androi.api.chitietbook.ChapterContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object BookDownloadManager {
    private val client = OkHttpClient()

    suspend fun downloadAndSaveChapter(
        context: Context,
        bookId: Long,
        chapterId: Long,
        content: ChapterContent
    ): ChapterEntity = withContext(Dispatchers.IO) {
        var savedImagePaths: String? = null

        if (content.contentType == "COMIC" && !content.imageUrls.isNullOrEmpty()) {
            val pathsList = mutableListOf<String>()
            val storageDir = File(context.filesDir, "offline_books/$bookId/$chapterId")
            if (!storageDir.exists()) storageDir.mkdirs()

            content.imageUrls.forEachIndexed { index, url ->
                val imageFile = File(storageDir, "page_$index.jpg")
                try {
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.byteStream()?.use { inputStream ->
                                FileOutputStream(imageFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            pathsList.add(imageFile.absolutePath)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            savedImagePaths = pathsList.joinToString(",")
        }

        return@withContext ChapterEntity(
            chapterId = chapterId,
            bookId = bookId,
            title = content.title ?: "Không có tiêu đề",
            contentType = content.contentType ?: "NOVEL",
            textContent = content.textContent,
            localImagePaths = savedImagePaths
        )
    }
}