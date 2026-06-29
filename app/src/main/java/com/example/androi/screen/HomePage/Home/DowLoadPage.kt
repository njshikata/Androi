package com.example.androi.screen.HomePage.Home

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // 👉 Import Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.BookEntity
import com.example.androi.api.api_tong
import com.example.androi.api.caterory.CateroryDTO
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DowLoadPage(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    val bookDao = remember { database.bookDao() }
    val chapterDao = remember { database.chapterDao() }
    val readingDao = remember { database.readingDao() }

    val downloadedBooks by bookDao.getAllDownloadedBooks().collectAsState(initial = emptyList())
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            categoryList = api_tong.cateroryApi.getCategories()
        } catch (e: Exception) { e.printStackTrace() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.65f)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⛩️ Thể Loại",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Đóng menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    categoryList.forEach { category ->
                        NavigationDrawerItem(
                            label = { Text(text = category.name ?: "Chưa rõ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                val catId = category.id
                                val catName = category.name
                                if (catId != null && catName != null) {
                                    navController.navigate("category_book/$catId/${Uri.encode(catName)}")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        // 👉 BỌC BOX VÀ THÊM ẢNH NỀN
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://i.pinimg.com/1200x/18/1a/f8/181af8e0927227f96bd07c3405cff202.jpg",
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )

            // Scaffold cần phải trong suốt để thấy ảnh nền ở dưới
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Narbar(
                        title = "Truyện Tải Về",
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            ) { innerPadding ->
                if (downloadedBooks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa tải truyện nào về máy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(downloadedBooks) { book ->
                            DownloadedBookItem(
                                book = book,
                                onClick = {
                                    coroutineScope.launch {
                                        val localChapters = chapterDao.getChaptersByBook(book.bookId)

                                        if (localChapters.isNotEmpty()) {
                                            val history = readingDao.getHistoryByBook(book.bookId)
                                            val targetChapterId = history?.lastChapterId ?: localChapters.first().chapterId
                                            navController.navigate("read_chapter/${book.bookId}/$targetChapterId?isOffline=true")
                                        } else {
                                            Toast.makeText(context, "Truyện này chưa tải xong hoặc lỗi dữ liệu!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onDelete = {
                                    bookToDelete = book
                                }
                            )
                        }
                    }
                }

                // DIALOG XÁC NHẬN XÓA
                if (bookToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { bookToDelete = null },
                        title = { Text(text = "Xác nhận xóa") },
                        text = { Text(text = "Bạn có chắc chắn muốn xóa truyện '${bookToDelete?.title}' khỏi máy không?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        bookToDelete?.let { book ->
                                            chapterDao.deleteChaptersByBook(book.bookId)
                                            bookDao.deleteBook(book)
                                            Toast.makeText(context, "Đã xóa truyện", Toast.LENGTH_SHORT).show()
                                        }
                                        bookToDelete = null
                                    }
                                }
                            ) {
                                Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { bookToDelete = null }) { Text("Hủy") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadedBookItem(book: BookEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)), // Đục nhẹ card lên xíu để dễ đọc
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(60.dp).height(85.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = book.author, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${book.totalChapters} chương đã tải", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}