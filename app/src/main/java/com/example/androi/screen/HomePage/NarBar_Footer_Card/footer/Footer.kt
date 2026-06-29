package com.example.androi.screen.HomePage.NarBar_Footer_Card.footer

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun Footer(navController: NavController) { // 👉 THÊM NAVCONTROLLER VÀO ĐÂY
    // Theo dõi xem mình đang ở màn hình nào để tô sáng đúng tab đó
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf("Trang chủ", "Thể loại", "Tải Về")
    // 👉 SỬA LẠI: Đổi "profile" thành "download_page"
    val routes = listOf("home", "category_page", "download_page")

    // (Gợi ý nhỏ: Bạn có thể đổi Icons.Filled.Person thành biểu tượng tải xuống nếu muốn)
    val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Person)

    NavigationBar(
        modifier = Modifier.height(64.dp)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(text = item, fontSize = 11.sp) },
                // Sáng lên nếu route hiện tại trùng với route của tab
                selected = currentRoute == routes[index],
                onClick = {
                    val targetRoute = routes[index]
                    // Ngăn chặn việc bấm liên tục vào 1 tab đang đứng
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            // Xóa sạch các trang lẻ tẻ, quay thẳng về màn hình chính, tránh đầy bộ nhớ
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true // Không mở đè nhiều trang giống nhau
                            restoreState = true // Giữ lại trạng thái lướt cũ
                        }
                    }
                }
            )
        }
    }
}