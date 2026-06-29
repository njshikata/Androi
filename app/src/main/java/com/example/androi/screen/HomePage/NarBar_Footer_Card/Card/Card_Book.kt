package com.example.androi.screen.HomePage.NarBar_Footer_Card.Card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// BỔ SUNG IMPORT COIL
import coil.compose.AsyncImage

@Composable
fun Card_Book(
    title: String = "Tên Truyện",
    chapter: String = "Chương 1",
    imageUrl: String? = null, // BỔ SUNG: Tham số nhận đường link ảnh
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(115.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        // --- PHẦN 1: TẤM ẢNH BÌA (COVER) ĐÃ SỬA BẰNG COIL ---
        // Nếu có link ảnh thì dùng AsyncImage của Coil để load
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Bìa truyện $title",
                contentScale = ContentScale.Crop, // Cắt cúp ảnh cho lấp đầy khung hình
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // Rơi vào đây nếu truyện chưa cập nhật ảnh bìa (giữ nguyên Box xám cũ dự phòng)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(colors = listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO COVER",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- PHẦN 2: THÔNG TIN TRUYỆN ---
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = chapter,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}