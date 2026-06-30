package com.example.androi.screen.HomePage.Home

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import androidx.compose.foundation.lazy.LazyColumn

// Import các API và DTO
import com.example.androi.api.api_tong
import com.example.androi.api.book_Home.Book_Home_Dto
import com.example.androi.api.caterory.CateroryDTO

// Import các Component
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Book
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Caterory_Hompage
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar

@Composable
fun Home_Screen(navController: NavController) {
    val context = LocalContext.current

    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }
    var trendingComics by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var trendingNovels by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var newestBooks by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var recentlyUpdated by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    val isOnline by rememberNetworkConnectivityState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var isSearching by remember { mutableStateOf(false) }
    var filteredBooks by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            isSearching = false
        } else {
            isSearching = true
            val allBooks = trendingComics + trendingNovels + newestBooks + recentlyUpdated
            filteredBooks = allBooks.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }.distinctBy { it.id }
        }
    }

    // GỌI API LẠI MỖI KHI CÓ MẠNG (isOnline = true)
    LaunchedEffect(isOnline) {
        if (isOnline) {
            isLoading = true
            try {
                withTimeout(30000L) { // Timeout 30 giây
                    coroutineScope {
                        val catDeferred = async { api_tong.getCateroryApi(context).getCategories() }
                        val comicDeferred = async { api_tong.getBookHomeApi(context).getTrendingComics() }
                        val novelDeferred = async { api_tong.getBookHomeApi(context).getTrendingNovels() }
                        val newDeferred = async { api_tong.getBookHomeApi(context).getNewestBooks() }
                        val updatedDeferred = async { api_tong.getBookHomeApi(context).getRecentlyUpdatedBooks() }

                        categoryList = catDeferred.await()
                        trendingComics = comicDeferred.await()
                        trendingNovels = novelDeferred.await()
                        newestBooks = newDeferred.await()
                        recentlyUpdated = updatedDeferred.await()
                    }
                }
            } catch (e: Exception) {
                println("Lỗi kết nối API Homepage: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://i.pinimg.com/1200x/18/1a/f8/181af8e0927227f96bd07c3405cff202.jpg",
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Narbar(
                    title = "Book Sagari",
                    navController = navController, // 👉 THÊM DÒNG NÀY VÀO ĐÂY LÀ XONG
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onSearchAction = { query -> performSearch(query) },
                    onLoginClick = { navController.navigate("login_screen") }
                )

                // XỬ LÝ GIAO DIỆN THEO TRẠNG THÁI MẠNG VÀ LOADING
                if (!isOnline) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Offline",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang không có kết nối Internet!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hệ thống sẽ tự động tải lại khi có mạng.\nBạn có muốn vào đọc sách đã tải không?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    navController.navigate("download_page") {
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text("Đến Trang Tải Về")
                            }
                        }
                    }
                } else if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (isSearching) {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("Kết quả tìm kiếm (${filteredBooks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(filteredBooks) { book ->
                                // 👉 SỬ DỤNG ĐIỂM THẬT VÀ LÀM TRÒN
                                val ratingFormatted = String.format("%.1f", book.averageRating ?: 0.0)

                                Card_Book(
                                    title = book.title ?: "",
                                    chapter = "⭐ $ratingFormatted | Chap ${book.totalChapters ?: 0}",
                                    imageUrl = book.coverImageUrl,
                                    onClick = { navController.navigate("book_detail/${book.id}") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (filteredBooks.isEmpty()) {
                                item { Text("Không tìm thấy truyện nào!", modifier = Modifier.padding(16.dp)) }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (categoryList.isNotEmpty()) {
                                Text("Thể Loại Nổi Bật", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(end = 16.dp)) {
                                    items(categoryList) { category ->
                                        Card_Caterory_Hompage(
                                            categoryName = category.name ?: "Chưa rõ",
                                            imageUrl = category.iconUrl,
                                            onClick = { navController.navigate("category_book/${category.id}/${Uri.encode(category.name)}") }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            BookListSection(navController, "⛩️ Top Truyện Tranh Nổi Bật", trendingComics)
                            BookListSection(navController, "🔮 Top Tiểu Thuyết Nổi Bật", trendingNovels)
                            BookListSection(navController, "🌟 Vừa Mới Ra Mắt", newestBooks)
                            BookListSection(navController, "🕊️ Mới Cập Nhật Chap", recentlyUpdated)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListSection(
    navController: NavController,
    sectionTitle: String,
    bookList: List<Book_Home_Dto>
) {
    if (bookList.isNotEmpty()) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(bookList) { book ->

                // 👉 SỬ DỤNG ĐIỂM THẬT VÀ LÀM TRÒN Ở ĐÂY
                val ratingFormatted = String.format("%.1f", book.averageRating ?: 0.0)

                Card_Book(
                    title = book.title ?: "Đang cập nhật...",
                    chapter = "⭐ $ratingFormatted | Chap ${book.totalChapters ?: 0}",
                    imageUrl = book.coverImageUrl,
                    onClick = {
                        navController.navigate("book_detail/${book.id}")
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}