package com.example.androi.screen.HomePage.Home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.BookEntity
import com.example.androi.api.api_tong
import com.example.androi.api.book_Home.Book_Detail_Dto
import com.example.androi.api.caterory.CateroryDTO
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DowLoadPage(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    var isLoggedIn by remember { mutableStateOf(false) }

    // Dữ liệu User
    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val token = sharedPreferences.getString("ACCESS_TOKEN", null)
        isLoggedIn = !token.isNullOrEmpty()

        if (isLoggedIn && token != null) {
            try {
                val response = api_tong.getLoginApi(context).getMe("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    username = response.body()!!.username
                    sharedPreferences.edit().putString("USERNAME", username).apply()
                } else {
                    username = sharedPreferences.getString("USERNAME", "Thành viên Sagari") ?: "Thành viên Sagari"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                username = sharedPreferences.getString("USERNAME", "Thành viên Sagari") ?: "Thành viên Sagari"
            }
        }
    }

    val bookDao = remember { database.bookDao() }
    val chapterDao = remember { database.chapterDao() }
    val readingDao = remember { database.readingDao() }

    val downloadedBooks by bookDao.getAllDownloadedBooks().collectAsState(initial = emptyList())
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    // Quản lý Tab
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Truyện tải về", "Đang theo dõi")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            categoryList = api_tong.getCateroryApi(context).getCategories()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Biến lưu danh sách theo dõi
    val bookPrefs = context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE)
    var subscribedBooks by remember { mutableStateOf<List<Pair<Long, Book_Detail_Dto>>>(emptyList()) }
    var isLoadingSubscribed by remember { mutableStateOf(false) }

    // Tự động quét và tải danh sách khi sang Tab "Đang theo dõi"
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            isLoadingSubscribed = true
            coroutineScope.launch(Dispatchers.IO) {
                val ids = bookPrefs.all.filter { it.key.startsWith("sub_") && it.value == true }
                    .mapNotNull { it.key.removePrefix("sub_").toLongOrNull() }

                val booksList = mutableListOf<Pair<Long, Book_Detail_Dto>>()
                for (id in ids) {
                    try {
                        val bookDetail = api_tong.getBookDetailApi(context).getBookDetail(id)
                        if (bookDetail != null) {
                            booksList.add(Pair(id, bookDetail))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    subscribedBooks = booksList
                    isLoadingSubscribed = false
                }
            }
        }
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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Narbar(
                        title = "Hồ sơ cá nhân",
                        navController = navController,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onLoginClick = { navController.navigate("login_screen") }
                    )
                }
            ) { innerPadding ->
                if (!isLoggedIn) {
                    // ====== MÀN HÌNH CHƯA ĐĂNG NHẬP ======
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Box(
                                modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "Lưu trữ truyện yêu thích", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Đăng nhập ngay để quản lý thư viện truyện tải về và đồng bộ tiến trình đọc của bạn.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { navController.navigate("login_screen") },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("Đăng nhập hoặc Đăng ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                } else {
                    // ====== MÀN HÌNH ĐÃ ĐĂNG NHẬP ======
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                        // 1. HEADER NGƯỜI DÙNG (THU GỌN SIÊU ĐẸP)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar nhỏ gọn
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = username.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Thông tin Text
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Thành viên Sagari",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Nút Đăng xuất
                                IconButton(
                                    onClick = {
                                        sharedPreferences.edit().remove("ACCESS_TOKEN").apply()
                                        isLoggedIn = false
                                        Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ExitToApp,
                                        contentDescription = "Đăng xuất",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // 2. TABS ĐIỀU HƯỚNG
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f)) }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        // 3. NỘI DUNG THEO TAB
                        when (selectedTabIndex) {
                            0 -> { // Tab: Truyện Tải Về
                                if (downloadedBooks.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("📭", fontSize = 48.sp)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("Thư viện trống.", color = Color.Gray, fontWeight = FontWeight.Medium)
                                            Text("Bạn chưa tải truyện nào về máy.", color = Color.LightGray, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                                onDelete = { bookToDelete = book }
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> { // Tab: Đang theo dõi
                                if (isLoadingSubscribed) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else if (subscribedBooks.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🔔", fontSize = 48.sp)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("Chưa theo dõi truyện nào.", color = Color.Gray, fontWeight = FontWeight.Medium)
                                            Text("Bật chuông trong phần đọc truyện để nhận thông báo nhé.", color = Color.LightGray, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(subscribedBooks) { (id, book) ->
                                            SubscribedBookItem(
                                                bookId = id,
                                                book = book,
                                                onClick = { navController.navigate("book_detail/$id") },
                                                onUnsubscribe = {
                                                    bookPrefs.edit().putBoolean("sub_$id", false).apply()
                                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("book_$id")
                                                    subscribedBooks = subscribedBooks.filter { it.first != id }
                                                    Toast.makeText(context, "Đã tắt thông báo", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // DIALOG XÁC NHẬN XÓA
                if (bookToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { bookToDelete = null },
                        title = { Text(text = "Xóa khỏi máy") },
                        text = { Text(text = "Bạn có chắc chắn muốn xóa bản offline của truyện '${bookToDelete?.title}' không?") },
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
                            TextButton(onClick = { bookToDelete = null }) { Text("Hủy", color = Color.Gray) }
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

// Giao diện list cho Tab 1 (Tải về) - Thiết kế Card nổi (Tự thích ứng Theme Sáng/Tối)
@Composable
fun DownloadedBookItem(book: BookEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverUrl, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.width(64.dp).height(90.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Đã fix lỗi TextOverflow ở đây
                Text(
                    text = book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = book.author, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Đã tải ${book.totalChapters} chương", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            }
        }
    }
}

// Giao diện list cho Tab 2 (Đang theo dõi) - Thiết kế Card nổi (Tự thích ứng Theme Sáng/Tối)
@Composable
fun SubscribedBookItem(bookId: Long, book: Book_Detail_Dto, onClick: () -> Unit, onUnsubscribe: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverImageUrl, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.width(64.dp).height(90.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Đã fix lỗi TextOverflow ở đây
                Text(
                    text = book.title ?: "Không có tựa đề",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = book.author ?: "Đang cập nhật", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Đang theo dõi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onUnsubscribe, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.NotificationsOff, contentDescription = "Hủy theo dõi", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}