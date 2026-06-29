package com.example.androi.screen.HomePage.Home.Caterory_Book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Import các API và DTO
import com.example.androi.api.api_tong
import com.example.androi.api.book_Home.Book_Home_Dto
import com.example.androi.api.caterory.CateroryDTO

// Import Component
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Book
// 👉 IMPORT CARD VIÊN THUỐC
import com.example.androi.screen.HomePage.NarBar_Footer_Card.Card.Card_Caterory_Hompage
import com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar.Narbar

@Composable
fun CategoryBook_Screen(
    navController: NavController,
    initialCategoryId: Int,
    initialCategoryName: String
) {
    var categoryList by remember { mutableStateOf<List<CateroryDTO>>(emptyList()) }
    var bookList by remember { mutableStateOf<List<Book_Home_Dto>>(emptyList()) }

    var currentCategoryId by remember { mutableIntStateOf(initialCategoryId) }
    var currentCategoryName by remember { mutableStateOf(initialCategoryName) }

    LaunchedEffect(Unit) {
        try {
            categoryList = api_tong.cateroryApi.getCategories()
        } catch (e: Exception) {
            println("Lỗi tải danh sách thể loại: ${e.message}")
        }
    }

    LaunchedEffect(currentCategoryId) {
        try {
            bookList = emptyList()
            bookList = api_tong.bookHomeApi.getBooksByCategory(currentCategoryId)
        } catch (e: Exception) {
            println("Lỗi tải truyện: ${e.message}")
        }
    }

    // VẼ GIAO DIỆN
    Column(modifier = Modifier.fillMaxSize()) {

        Narbar(
            title = "Thể loại: $currentCategoryName",
            onBackClick = { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PHẦN 1: THANH TRƯỢT THỂ LOẠI ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categoryList) { category ->
                val catId = category.id ?: return@items
                val catName = category.name ?: "Chưa rõ"

                // 👉 THAY THẾ BẰNG CARD CỦA HOMEPAGE VÀ TRUYỀN ẢNH VÀO
                Card_Caterory_Hompage(
                    categoryName = catName,
                    imageUrl = category.iconUrl,
                    onClick = {
                        currentCategoryId = catId
                        currentCategoryName = catName
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Có ${bookList.size} truyện thuộc thể loại $currentCategoryName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PHẦN 2: LƯỚI TRUYỆN ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(bookList) { book ->
                Card_Book(
                    title = book.title ?: "Đang cập nhật...",
                    chapter = "Chap ${book.totalChapters ?: 0}",
                    imageUrl = book.coverImageUrl, // 👉 TRUYỀN ẢNH BÌA TRUYỆN VÀO ĐÂY
                    onClick = {
                        navController.navigate("book_detail/${book.id}")
                    }
                )
            }
        }
    }
}