package com.example.androi.screen.HomePage.NarBar_Footer_Card.Card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun Card_Caterory(
    categoryName: String,
    onClick: () -> Unit = {}
) {
    // Danh sách các cặp màu Gradient để tạo sự đa dạng
    val gradientColors = listOf(
        Brush.verticalGradient(colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))),
        Brush.verticalGradient(colors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
        Brush.verticalGradient(colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
        Brush.verticalGradient(colors = listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
    )

    // Chọn màu dựa trên độ dài tên category (để mỗi ô có màu khác nhau)
    val randomGradient = gradientColors[categoryName.length % gradientColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), // Bo góc mềm mại hơn
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Đổ bóng đậm hơn
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(randomGradient), // Dùng Gradient làm nền
            contentAlignment = Alignment.Center
        ) {
            // Tên thể loại - Để font to và chữ in đậm, đổ bóng nhẹ
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge, // Font to hơn
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}