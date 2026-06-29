package com.example.androi.screen.HomePage.Home

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage // 👉 Thêm import Coil
import kotlinx.coroutines.launch

// Import API và Component
import com.example.androi.api.api_tong
import com.example.androi.api.caterory.CateroryDTO
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Caterory
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar

@Composable
fun CateroryPage(navController: NavController) {
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 👉 STATE CHO MENU TRƯỢT
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            categoryList = api_tong.cateroryApi.getCategories()
        } catch (e: Exception) {
            println("Lỗi tải danh sách thể loại: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // 👉 BỌC TOÀN BỘ BẰNG MENU TRƯỢT GIỐNG HOMEPAGE
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
        // 👉 BỌC NỘI DUNG VÀO BOX ĐỂ LÀM ẢNH NỀN
        Box(modifier = Modifier.fillMaxSize()) {
            // --- ẢNH NỀN TỪ LINK PINTEREST ---
            AsyncImage(
                model = "https://i.pinimg.com/1200x/18/1a/f8/181af8e0927227f96bd07c3405cff202.jpg",
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop, // Cắt ảnh cho lấp đầy màn hình
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f // Giảm độ rõ nét xuống 30% để không làm chói mắt và làm chìm nội dung chính
            )

            // --- NỘI DUNG GIAO DIỆN CHÍNH NẰM ĐÈ LÊN ẢNH NỀN ---
            Column(modifier = Modifier.fillMaxSize()) {
                // TRUYỀN SỰ KIỆN MỞ MENU VÀO NARBAR
                Narbar(
                    title = "Tất Cả Thể Loại",
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (categoryList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có thể loại nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categoryList) { category ->
                            Card_Caterory(
                                categoryName = category.name ?: "Chưa rõ",
//                                imageUrl = category.iconUrl,
                                onClick = {
                                    val catId = category.id
                                    val catName = category.name
                                    if (catId != null && catName != null) {
                                        navController.navigate("category_book/$catId/${Uri.encode(catName)}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}