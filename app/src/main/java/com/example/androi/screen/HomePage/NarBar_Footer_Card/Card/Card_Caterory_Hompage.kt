package com.example.androi.screen.HomePage.NarBar_Footer_Card.Card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Card_Caterory_Hompage(
    categoryName: String,
    imageUrl: String? = null, // Vẫn giữ tham số này để file Home_Screen không báo lỗi
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .padding(end = 12.dp)
            .height(38.dp) // Hạ chiều cao xuống 38dp cho viên thuốc nhìn thanh thoát hơn
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        // Dùng Box thay vì Row, ép contentAlignment = Center để chữ nằm chính giữa tuyệt đối
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp) // Padding đều 2 bên trái phải
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}