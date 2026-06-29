package com.example.androi.screen.HomePage.Home

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale // 👉 Import ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage // 👉 Import Coil AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Clear

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
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }
    var trendingComics by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var trendingNovels by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var newestBooks by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }
    var recentlyUpdated by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }

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

    LaunchedEffect(Unit) {
        try {
            coroutineScope {
                val catDeferred = async { api_tong.cateroryApi.getCategories() }
                val comicDeferred = async { api_tong.bookHomeApi.getTrendingComics() }
                val novelDeferred = async { api_tong.bookHomeApi.getTrendingNovels() }
                val newDeferred = async { api_tong.bookHomeApi.getNewestBooks() }
                val updatedDeferred = async { api_tong.bookHomeApi.getRecentlyUpdatedBooks() }

                categoryList = catDeferred.await()
                trendingComics = comicDeferred.await()
                trendingNovels = novelDeferred.await()
                newestBooks = newDeferred.await()
                recentlyUpdated = updatedDeferred.await()
            }
        } catch (e: Exception) {
            println("Lỗi kết nối API Homepage: ${e.message}")
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
        // 👉 BỌC BOX VÀ THÊM ẢNH NỀN
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
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onSearchAction = { query -> performSearch(query) }
                )

                if (isSearching) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        item {
                            Text("Kết quả tìm kiếm (${filteredBooks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(filteredBooks) { book ->
                            Card_Book(
                                title = book.title ?: "",
                                chapter = "Chap ${book.totalChapters}",
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
                Card_Book(
                    title = book.title ?: "Đang cập nhật...",
                    chapter = "Chap ${book.totalChapters ?: 0}",
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