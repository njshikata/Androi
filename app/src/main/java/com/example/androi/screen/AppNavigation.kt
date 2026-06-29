package com.example.androi.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.example.androi.screen.HomePage.NarBar_Footer_Card.footer.Footer

// 👉 BẠN NHỚ IMPORT FILE CateroryPage VỪA TẠO VÀO NHÉ

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        // 👉 TRUYỀN navController XUỐNG CHỖ NÀY
        bottomBar = { Footer(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
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
            // 👉 THÊM ROUTE CHO TRANG TẢI VỀ Ở ĐÂY
            composable("download_page") {
                DowLoadPage(navController = navController)
            }
            // --- ROUTE 2: TRANG CHI TIẾT THỂ LOẠI (Danh sách truyện của 1 thể loại) ---
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
                // 1. Thêm ?isOffline={isOffline} vào cuối route
                route = "read_chapter/{bookId}/{chapterId}?isOffline={isOffline}",
                arguments = listOf(
                    navArgument("bookId") { type = NavType.LongType },
                    navArgument("chapterId") { type = NavType.LongType },
                    // 2. Khai báo thêm argument isOffline (mặc định là false)
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
                    isOffline = isOffline // 3. Truyền biến này vào UI
                )
            }
        }
    }
}