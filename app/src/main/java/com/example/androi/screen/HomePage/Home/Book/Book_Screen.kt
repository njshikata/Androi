package com.example.androi.screen.HomePage.Home.Book

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
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
import com.example.androi.Review.ReviewRequest
import com.example.androi.Review.ReviewResponse
import com.example.androi.api.api_tong
import com.example.androi.api.book_Home.Book_Detail_Dto
import com.example.androi.api.chitietbook.ChapterSummary
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.BookEntity
import com.example.androi.SQL.BookDownloadManager
import com.google.firebase.messaging.FirebaseMessaging
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
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var reviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }
    var isReviewsExpanded by remember { mutableStateOf(false) }
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else 0f

    val downloadedBooks by database.bookDao().getAllDownloadedBooks().collectAsState(initial = emptyList())
    val isDownloaded = downloadedBooks.any { it.bookId == bookId }

    val prefs = context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE)
    var isSubscribed by remember { mutableStateOf(prefs.getBoolean("sub_$bookId", false)) }

    LaunchedEffect(bookId) {
        try {
            val history = database.readingDao().getHistoryByBook(bookId)
            if (history != null) {
                lastReadChapterId = history.lastChapterId
            }
        } catch (e: Exception) { e.printStackTrace() }

        try {
            val res = api_tong.getReviewApi(context).getReviewsByBook(bookId)
            if (res.isSuccessful && res.body() != null) {
                reviews = res.body()!!
            }
        } catch (e: Exception) { e.printStackTrace() }

        try {
            bookDetail = api_tong.getBookDetailApi(context).getBookDetail(bookId)
        } catch (e: Exception) {
            println("Lỗi tải chi tiết truyện: ${e.message}")
        } finally {
            isLoading = false
        }

        api_tong.getBookReadApi(context).getChapters(bookId).enqueue(object : Callback<List<ChapterSummary>> {
            override fun onResponse(call: Call<List<ChapterSummary>>, response: Response<List<ChapterSummary>>) {
                if (response.isSuccessful && response.body() != null) {
                    chapterList = response.body()!!
                }
            }
            override fun onFailure(call: Call<List<ChapterSummary>>, t: Throwable) {
                println("Lỗi tải danh sách chương: ${t.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Narbar(
            title = bookDetail?.title ?: "Chi tiết truyện",
            navController = navController,
            onBackClick = { navController.popBackStack() }
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (bookDetail != null) {
            val book = bookDetail!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // --- THÔNG TIN TRUYỆN ---
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
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (avgRating > 0) String.format("%.1f/5", avgRating) else "Chưa có đánh giá",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- NÚT HÀNH ĐỘNG ---
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
                                                val response = api_tong.getBookReadApi(context).readChapter(chapter.id).execute()
                                                if (response.isSuccessful && response.body() != null) {
                                                    val chapterEntity = BookDownloadManager.downloadAndSaveChapter(
                                                        context = context,
                                                        bookId = bookId,
                                                        chapterId = chapter.id,
                                                        content = response.body()!!
                                                    )
                                                    database.chapterDao().insertChapter(chapterEntity)
                                                }
                                            } catch (e: Exception) { e.printStackTrace() }
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
                                FirebaseMessaging.getInstance().subscribeToTopic("book_$bookId")
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) { Toast.makeText(context, "Đã bật nhận thông báo chương mới!", Toast.LENGTH_SHORT).show() }
                                    }
                            } else {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("book_$bookId")
                                Toast.makeText(context, "Đã tắt thông báo.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
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

                // --- THANH CLICK ĐỂ SỔ KHUNG ĐÁNH GIÁ ---
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isReviewsExpanded = !isReviewsExpanded },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF57C00))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Đánh giá & Bình luận (${reviews.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Icon(
                            imageVector = if (isReviewsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // KHUNG BÌNH LUẬN NỘI TUYẾN
                AnimatedVisibility(
                    visible = isReviewsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    InlineReviewSection(
                        bookId = bookId,
                        initialReviews = reviews,
                        onReviewAdded = { newReview ->
                            reviews = listOf(newReview) + reviews
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- DANH SÁCH CHƯƠNG ---
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

// 👉 KHUNG BÌNH LUẬN - ĐÃ CHUẨN HÓA MÀU SẮC THEO THEME
@Composable
fun InlineReviewSection(
    bookId: Long,
    initialReviews: List<ReviewResponse>,
    onReviewAdded: (ReviewResponse) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var rating by remember { mutableIntStateOf(5) }
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val sharedPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val token = sharedPrefs.getString("ACCESS_TOKEN", null)
    val isLoggedIn = !token.isNullOrEmpty()

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {

        // --- KHU VỰC NHẬP BÌNH LUẬN ---
        if (isLoggedIn) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Đánh giá của bạn: ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Màu vàng sao giữ nguyên
                            modifier = Modifier.size(22.dp).clickable { rating = i }.padding(end = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        // 👉 Dùng màu nền động theo Theme thay vì fix cứng mã Hex
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier.weight(1f),
                            // 👉 Màu chữ động theo Theme
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                if (content.isEmpty()) Text("Thêm bình luận...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                innerTextField()
                            }
                        )

                        if (content.isNotBlank()) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Gửi",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp).clickable {
                                        isSubmitting = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                val req = com.example.androi.Review.ReviewRequest(bookId = bookId, rating = rating, content = content)
                                                val response = api_tong.getReviewApi(context).addReview(token = "Bearer $token", request = req)
                                                withContext(Dispatchers.Main) {
                                                    if (response.isSuccessful && response.body() != null) {
                                                        onReviewAdded(response.body()!!)
                                                        content = ""
                                                        rating = 5
                                                    } else {
                                                        Toast.makeText(context, "Lỗi gửi đánh giá!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) { Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show() }
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                Text(text = "Vui lòng đăng nhập để bình luận", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }

        // --- DANH SÁCH BÌNH LUẬN ĐÃ CÓ ---
        if (initialReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text("Chưa có đánh giá nào. Hãy là người đầu tiên!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        } else {
            initialReviews.forEach { review ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "@${review.username}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = review.content, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}