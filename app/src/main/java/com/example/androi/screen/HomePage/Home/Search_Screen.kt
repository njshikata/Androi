package com.example.androi.screen.HomePage.Home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search_Screen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) } // Trạng thái SearchBar đang mở

    // Giả lập lịch sử tìm kiếm lưu trong máy
    var searchHistory by remember { mutableStateOf(listOf("Tiên hiệp", "Kiếm hiệp", "Đấu phá thương khung")) }

    // Giả lập kết quả tìm kiếm từ API
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Logic giả lập: Gõ phím xong đợi 0.5s mới gọi API (Debounce) để đỡ lag server
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        isSearching = true
        delay(500) // Đợi user gõ xong

        // TODO: Ở ĐÂY BẠN GỌI API TÌM KIẾM TỪ BACKEND
        // searchResults = api_tong.bookApi.searchBook(query)

        // Giả lập kết quả trả về
        searchResults = listOf("Truyện $query 1", "Truyện $query 2", "Kết quả khác cho $query")
        isSearching = false
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // DÙNG SEARCH BAR XỊN CỦA MATERIAL 3
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    // Khi user bấm phím Enter/Kính lúp trên bàn phím
                    active = false
                    if (query.isNotBlank() && !searchHistory.contains(query)) {
                        searchHistory = listOf(query) + searchHistory
                    }
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Tìm kiếm truyện, tác giả...") },
                leadingIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- PHẦN BÊN TRONG SEARCH BAR KHI ĐANG ACTIVE ---

                if (query.isEmpty()) {
                    // 1. NẾU CHƯA GÕ GÌ -> HIỆN LỊCH SỬ TÌM KIẾM
                    Text(
                        text = "Lịch sử tìm kiếm",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn {
                        items(searchHistory) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query = historyItem // Bấm lịch sử thì tự điền chữ lên thanh search
                                    }
                                    .padding(16.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
                                Text(text = historyItem)
                            }
                        }
                    }
                } else {
                    // 2. NẾU ĐANG GÕ -> HIỆN KẾT QUẢ GỢI Ý / HOẶC LOADING
                    if (isSearching) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    LazyColumn {
                        items(searchResults) { result ->
                            ListItem(
                                headlineContent = { Text(result) },
                                leadingContent = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    // Chuyển hướng sang trang chi tiết truyện
                                    println("Đã chọn: $result")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}