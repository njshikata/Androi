package com.example.androi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashScreen() {
    // Load file json từ res/raw
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ghibli_tribute))

    // Dùng Column để xếp dọc Animation ở trên, Chữ ở dưới
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Căn giữa toàn màn hình theo chiều dọc
        horizontalAlignment = Alignment.CenterHorizontally // Căn giữa theo chiều ngang
    ) {
        // 1. Animation Lottie
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(250.dp) // Thêm size để khống chế kích thước Lottie nếu nó quá to
        )

        Spacer(modifier = Modifier.height(24.dp)) // Tạo khoảng trống giữa hình và chữ

        // 2. Chữ thông báo chính
        Text(
            text = "Đang tải dữ liệu...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp)) // Khoảng trống nhỏ

        // 3. Chữ thông báo phụ (xin chờ)
        Text(
            text = "Vui lòng chờ trong giây lát",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}