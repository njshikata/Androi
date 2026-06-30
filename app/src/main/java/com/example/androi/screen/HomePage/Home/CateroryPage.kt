package com.example.androi.screen.HomePage.Home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning // Thêm icon Warning
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// Import API và Component
import com.example.androi.api.api_tong
import com.example.androi.api.caterory.CateroryDTO
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Caterory
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar

// 👉 Hàm theo dõi trạng thái mạng theo thời gian thực (Real-time)
@Composable
fun rememberNetworkConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = true) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Kiểm tra mạng lần đầu
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        value = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        // Lắng nghe thay đổi mạng (bật/tắt wifi, 4g, rớt mạng)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = true // Có mạng
            }
            override fun onLost(network: Network) {
                value = false // Mất mạng
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

@Composable
fun CateroryPage(navController: NavController) {
    val context = LocalContext.current
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 👉 Lấy trạng thái mạng trực tiếp
    val isOnline by rememberNetworkConnectivityState()

    // State cho menu trượt
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // 👉 LaunchedEffect này sẽ CHẠY LẠI mỗi khi isOnline thay đổi từ false -> true
    LaunchedEffect(isOnline) {
        if (isOnline) {
            isLoading = true
            try {
                // Timeout 30s nếu mạng có nhưng quá yếu
                withTimeout(30000L) {
                    // 👉 2. SỬA THÀNH getCateroryApi(context)
                    categoryList = api_tong.getCateroryApi(context).getCategories()
                }
            } catch (e: Exception) {
                println("Lỗi tải danh sách thể loại: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Giao diện chính (Bọc NavigationDrawer)
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
        Box(modifier = Modifier.fillMaxSize()) {
            // ẢNH NỀN
            AsyncImage(
                model = "https://i.pinimg.com/1200x/18/1a/f8/181af8e0927227f96bd07c3405cff202.jpg",
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // NARBAR
                Narbar(
                    title = "Tất Cả Thể Loại",
                    navController = navController, // 👉 THÊM DÒNG NÀY
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )

                // 👉 KIỂM TRA TRẠNG THÁI MẠNG ĐỂ HIỂN THỊ NỘI DUNG CHÍNH
                if (!isOnline) {
                    // MÀN HÌNH BÁO MẤT MẠNG
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
                                    // 👉 SỬA CHÍNH XÁC THÀNH "download_page" (Khớp với AppNavigation)
                                    navController.navigate("download_page") {
                                        // Thêm tùy chọn này để tránh mở trùng lặp nhiều trang tải về
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text("Đến Trang Tải Về")
                            }
                        }
                    }
                } else if (isLoading) {
                    // MÀN HÌNH LOADING KHI CÓ MẠNG ĐANG TẢI API
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (categoryList.isEmpty()) {
                    // MÀN HÌNH TRỐNG NẾU API KHÔNG CÓ DỮ LIỆU
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có thể loại nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // MÀN HÌNH DANH SÁCH THÀNH CÔNG
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