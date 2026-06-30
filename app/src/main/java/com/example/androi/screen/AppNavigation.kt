package com.example.androi.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Import các màn hình và Component
import com.example.androi.screen.HomePage.Home.Book.Book_Screen
import com.example.androi.screen.HomePage.Home.Book_Doc.BookReadScreen
import com.example.androi.screen.HomePage.Home.CateroryPage
import com.example.androi.screen.HomePage.Home.Home_Screen
import com.example.androi.screen.HomePage.Home.Caterory_Book.CategoryBook_Screen
import com.example.androi.screen.HomePage.Home.DowLoadPage
import com.example.androi.screen.HomePage.Home.LoginScreen
import com.example.androi.screen.HomePage.NarBar_Footer_Card.footer.Footer

@Composable
fun AppNavigation(
    startDestination: String = "home",
    targetBookId: Long = -1L // 👉 THÊM THAM SỐ NHẬN ID TỪ MAIN
) {
    val navController = rememberNavController()

    // 👉 ĐÂY LÀ CHÌA KHÓA: Tự động nhảy sang trang truyện nếu có ID
    LaunchedEffect(targetBookId) {
        if (targetBookId != -1L) {
            navController.navigate("book_detail/$targetBookId")
        }
    }

    Scaffold(
        bottomBar = { Footer(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination, // Vẫn giữ gốc chuẩn để Footer không bị ngáo
            modifier = Modifier.padding(innerPadding)
        ) {

            // --- ROUTE 1: TRANG CHỦ ---
            composable("home") {
                Home_Screen(navController = navController)
            }

            // --- ROUTE MỚI: TRANG THỂ LOẠI (Ở THANH FOOTER) ---
            composable("category_page") {
                CateroryPage(navController = navController)
            }

            // --- ROUTE TẢI VỀ & CÁ NHÂN ---
            composable("download_page") {
                DowLoadPage(navController = navController)
            }

            // --- ROUTE ĐĂNG NHẬP / ĐĂNG KÝ ---
            composable("login_screen") {
                LoginScreen(navController = navController)
            }

            // --- ROUTE 2: TRANG CHI TIẾT THỂ LOẠI ---
            composable(
                route = "category_book/{id}/{name}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("id") ?: 0
                val categoryName = backStackEntry.arguments?.getString("name") ?: ""

                CategoryBook_Screen(
                    navController = navController,
                    initialCategoryId = categoryId,
                    initialCategoryName = categoryName
                )
            }

            // --- ROUTE 3: TRANG CHI TIẾT TRUYỆN ---
            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L

                Book_Screen(
                    navController = navController,
                    bookId = bookId
                )
            }

            // --- ROUTE 4: MÀN HÌNH ĐỌC TRUYỆN ---
            composable(
                route = "read_chapter/{bookId}/{chapterId}?isOffline={isOffline}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType },
                    navArgument("chapterId") { type = NavType.LongType },
                    navArgument("isOffline") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                val chapterId = backStackEntry.arguments?.getLong("chapterId") ?: 0L
                val isOffline = backStackEntry.arguments?.getBoolean("isOffline") ?: false

                BookReadScreen(
                    navController = navController,
                    bookId = bookId,
                    initialChapterId = chapterId,
                    isOffline = isOffline
                )
            }
        }
    }
}