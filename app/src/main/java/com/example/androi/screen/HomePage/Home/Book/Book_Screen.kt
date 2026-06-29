package com.example.androi.screen.HomePage.Home.Book

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage

import com.example.androi.api.api_tong
import com.example.androi.api.book_Home.Book_Detail_Dto
import com.example.androi.api.chitietbook.ChapterSummary
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.BookEntity
import com.example.androi.SQL.enity.NotificationEntity
import com.example.androi.SQL.BookDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun showSystemNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "book_updates_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Thông báo truyện mới", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_popup_reminder) // Nhớ đổi icon cho phù hợp app
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

@Composable
fun Book_Screen(
    navController: NavController,
    bookId: Long
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    var bookDetail by remember { mutableStateOf<Book_Detail_Dto?>(null) }
    var chapterList by remember { mutableStateOf<List<ChapterSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var lastReadChapterId by remember { mutableStateOf<Long?>(null) }

    val downloadedBooks by database.bookDao().getAllDownloadedBooks().collectAsState(initial = emptyList())
    val isDownloaded = downloadedBooks.any { it.bookId == bookId }

    val prefs = context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE)
    var isSubscribed by remember { mutableStateOf(prefs.getBoolean("sub_$bookId", false)) }
    var lastKnownChapterCount by remember { mutableStateOf(prefs.getInt("count_$bookId", 0)) }

    LaunchedEffect(bookId) {
        try {
            val history = database.readingDao().getHistoryByBook(bookId)
            if (history != null) {
                lastReadChapterId = history.lastChapterId
            }
        } catch (e: Exception) { e.printStackTrace() }

        try {
            bookDetail = api_tong.bookDetailApi.getBookDetail(bookId)
        } catch (e: Exception) {
            println("Lỗi tải chi tiết truyện: ${e.message}")
        } finally {
            isLoading = false
        }

        api_tong.bookReadApi.getChapters(bookId).enqueue(object : Callback<List<ChapterSummary>> {
            override fun onResponse(call: Call<List<ChapterSummary>>, response: Response<List<ChapterSummary>>) {
                if (response.isSuccessful && response.body() != null) {
                    val fetchedChapters = response.body()!!
                    chapterList = fetchedChapters

                    if (isSubscribed) {
                        if (lastKnownChapterCount > 0 && fetchedChapters.size > lastKnownChapterCount) {
                            val newChapters = fetchedChapters.size - lastKnownChapterCount
                            val msgTitle = "🔥 ${bookDetail?.title ?: "Truyện bạn theo dõi"}"
                            val msgBody = "Vừa cập nhật $newChapters chương mới! Vào đọc ngay thôi."

                            // 1. Hiển thị thông báo ngoài màn hình
                            showSystemNotification(context = context, title = msgTitle, message = msgBody)

                            // 2. Lưu vào Room Database để Narbar đọc
                            coroutineScope.launch(Dispatchers.IO) {
                                database.notificationDao().insertNotification(
                                    NotificationEntity(
                                        bookId = bookId,
                                        title = msgTitle,
                                        message = msgBody
                                    )
                                )
                            }
                        }
                        prefs.edit().putInt("count_$bookId", fetchedChapters.size).apply()
                        lastKnownChapterCount = fetchedChapters.size
                    }
                }
            }
            override fun onFailure(call: Call<List<ChapterSummary>>, t: Throwable) {
                println("Lỗi tải danh sách chương: ${t.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Narbar(title = bookDetail?.title ?: "Chi tiết truyện", onBackClick = { navController.popBackStack() })

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (bookDetail != null) {
            val book = bookDetail!!

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (!book.coverImageUrl.isNullOrEmpty()) {
                        AsyncImage(model = book.coverImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.width(120.dp).aspectRatio(0.7f).clip(RoundedCornerShape(8.dp)))
                    } else {
                        Box(modifier = Modifier.width(120.dp).aspectRatio(0.7f).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = book.title ?: "Không có tựa đề", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Tác giả: ${book.author ?: "Đang cập nhật"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tình trạng: ${if (book.status == "ONGOING") "Đang ra" else "Hoàn thành"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (chapterList.isNotEmpty()) {
                                navController.navigate("read_chapter/$bookId/${chapterList.first().id}")
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = chapterList.isNotEmpty()
                    ) {
                        Text("ĐỌC TỪ ĐẦU", fontWeight = FontWeight.Bold)
                    }

                    if (lastReadChapterId != null) {
                        Button(
                            onClick = { navController.navigate("read_chapter/$bookId/$lastReadChapterId") },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("ĐỌC TIẾP", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (bookDetail != null && chapterList.isNotEmpty()) {
                                isDownloading = true
                                Toast.makeText(context, "Bắt đầu tải truyện, vui lòng đợi...", Toast.LENGTH_SHORT).show()

                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val entity = BookEntity(
                                            bookId = bookId,
                                            title = bookDetail!!.title ?: "Chưa rõ",
                                            coverUrl = bookDetail!!.coverImageUrl ?: "",
                                            author = bookDetail!!.author ?: "Đang cập nhật",
                                            totalChapters = chapterList.size
                                        )
                                        database.bookDao().insertBook(entity)

                                        for (chapter in chapterList) {
                                            try {
                                                val response = api_tong.bookReadApi.readChapter(chapter.id).execute()
                                                if (response.isSuccessful && response.body() != null) {
                                                    val chapterContent = response.body()!!
                                                    val chapterEntity = BookDownloadManager.downloadAndSaveChapter(
                                                        context = context,
                                                        bookId = bookId,
                                                        chapterId = chapter.id,
                                                        content = chapterContent
                                                    )
                                                    database.chapterDao().insertChapter(chapterEntity)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Tải truyện thành công!", Toast.LENGTH_LONG).show()
                                            isDownloading = false
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Lỗi tải về: ${e.message}", Toast.LENGTH_SHORT).show()
                                            isDownloading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isDownloading && !isDownloaded && chapterList.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(imageVector = if (isDownloaded) Icons.Filled.Check else Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(when {
                            isDownloaded -> "ĐÃ TẢI VỀ"
                            isDownloading -> "ĐANG TẢI..."
                            else -> "TẢI OFFLINE"
                        }, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            isSubscribed = !isSubscribed
                            prefs.edit().putBoolean("sub_$bookId", isSubscribed).apply()
                            if (isSubscribed) {
                                prefs.edit().putInt("count_$bookId", chapterList.size).apply()
                                lastKnownChapterCount = chapterList.size
                                Toast.makeText(context, "Đã bật nhận thông báo chương mới!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Đã tắt thông báo.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                            contentDescription = "Thông báo",
                            tint = if (isSubscribed) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Danh sách chương", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (chapterList.isEmpty()) {
                    Text(text = "Truyện này chưa có chương nào.", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                } else {
                    chapterList.forEach { chapter ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { navController.navigate("read_chapter/$bookId/${chapter.id}") },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = chapter.title,
                                    fontWeight = if (chapter.id == lastReadChapterId) FontWeight.Bold else FontWeight.Medium,
                                    color = if (chapter.id == lastReadChapterId) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                                if (chapter.isPremium) Text("Premium", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}