package com.example.androi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Quan trọng: import để dùng state
import androidx.compose.ui.Modifier
import com.example.androi.screen.AppNavigation
import com.example.androi.ui.theme.AndroiTheme
import kotlinx.coroutines.delay // Quan trọng: import để dùng delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroiTheme {
                // 1. Tạo biến để kiểm soát trạng thái
                var showSplash by remember { mutableStateOf(true) }

                // 2. Chạy logic chờ 5 giây rồi tắt splash
                LaunchedEffect(Unit) {
                    delay(5000) // 5000ms = 5 giây
                    showSplash = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Logic chuyển đổi màn hình
                    if (showSplash) {
                        SplashScreen() // Gọi từ file SplashScreen.kt
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }
}