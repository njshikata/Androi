package com.example.androi.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    onBackground = Color.White // Chữ trắng trên nền đen
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F)
)

// 👉 THÊM BẢNG MÀU BẢO VỆ MẮT (MÀU GIẤY CŨ)
private val EyeProtectColorScheme = lightColorScheme(
    primary = Color(0xFF6D4C41),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7CCC8),
    onPrimaryContainer = Color(0xFF3E2723),
    background = Color(0xFFF4ECD8),       // Màu nền chính (Vàng ấm)
    onBackground = Color(0xFF3E2723),     // Màu chữ (Nâu đậm đỡ chói)
    surface = Color(0xFFFFF6E5),
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFEBE0C8),
    onSurfaceVariant = Color(0xFF4E342E)
)

// 👉 HÀM LẮNG NGHE SỰ THAY ĐỔI CỦA CÀI ĐẶT
@Composable
fun getThemePreference(context: Context): State<String> {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val themeState = remember { mutableStateOf(prefs.getString("theme_mode", "LIGHT") ?: "LIGHT") }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "theme_mode") {
                themeState.value = sharedPreferences.getString(key, "LIGHT") ?: "LIGHT"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    return themeState
}

@Composable
fun AndroiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt dynamicColor để máy không tự đè màu hệ thống lên màu của mình
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Đọc trạng thái từ file cài đặt
    val themeMode by getThemePreference(context)

    // Xác định giao diện dựa trên lựa chọn
    val colorScheme = when (themeMode) {
        "DARK" -> DarkColorScheme
        "EYE_PROTECT" -> EyeProtectColorScheme
        "LIGHT" -> LightColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}