package com.example.androi.screen.HomePage.NarBar_Footer_Card.narbar

import android.content.Context
// 👉 Đã bổ sung import background ở đây
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 👉 BẮT BUỘC IMPORT CÁI NÀY
import androidx.navigation.NavController
import com.example.androi.SQL.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Narbar(
    title: String = "Book Sagari",
    navController: NavController? = null,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: () -> Unit = {},
    onSearchAction: (String) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    val appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val token = appPrefs.getString("ACCESS_TOKEN", null)
    val isLoggedIn = !token.isNullOrEmpty()

    val notifications by database.notificationDao().getAllNotifications().collectAsState(initial = emptyList())
    val unreadCount by database.notificationDao().getUnreadCount().collectAsState(initial = 0)

    val dateFormat = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())

    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var showNotificationMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var selectedTheme by remember { mutableStateOf(prefs.getString("theme_mode", "LIGHT") ?: "LIGHT") }

    if (isSearchMode) {
        TopAppBar(
            modifier = Modifier.height(56.dp),
            windowInsets = WindowInsets(0.dp),
            title = {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    // 👉 Dùng màu chữ động
                    textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                // 👉 Dùng màu chữ mờ động
                                Text("Nhập tên truyện...", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f), fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchAction(searchQuery) })
                )
            },
            navigationIcon = {
                IconButton(onClick = { isSearchMode = false; searchQuery = "" }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Đóng")
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Xóa")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    } else {
        TopAppBar(
            modifier = Modifier.height(56.dp),
            windowInsets = WindowInsets(0.dp),
            title = { Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Quay lại") }
                } else {
                    IconButton(onClick = onMenuClick) { Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu") }
                }
            },
            actions = {
                IconButton(onClick = { isSearchMode = true }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Tìm kiếm")
                }

                if (!isLoggedIn) {
                    IconButton(onClick = onLoginClick) {
                        Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "Đăng nhập")
                    }
                }

                Box {
                    IconButton(onClick = {
                        showNotificationMenu = !showNotificationMenu
                        if (showNotificationMenu && unreadCount > 0) {
                            coroutineScope.launch(Dispatchers.IO) { database.notificationDao().markAllAsRead() }
                        }
                    }) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Thông báo")
                        }
                    }

                    // 👉 Sửa màu nền và chữ của Dropdown Menu Thông báo
                    DropdownMenu(
                        expanded = showNotificationMenu,
                        onDismissRequest = { showNotificationMenu = false },
                        modifier = Modifier.width(300.dp).heightIn(max = 400.dp).background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (notifications.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có thông báo mới", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { showNotificationMenu = false }
                            )
                        } else {
                            notifications.forEach { notif ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = notif.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = dateFormat.format(Date(notif.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                        }
                                    },
                                    onClick = {
                                        showNotificationMenu = false
                                        if (notif.bookId > 0L) {
                                            navController?.navigate("book_detail/${notif.bookId}")
                                        }
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Thêm")
                    }
                    // 👉 Sửa màu nền Dropdown Menu Cài đặt
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cài đặt", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = {
                                showMoreMenu = false
                                showThemeDialog = true
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cài đặt giao diện", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column {
                    val themes = listOf(
                        "LIGHT" to "Chế độ Sáng",
                        "DARK" to "Chế độ Tối",
                        "EYE_PROTECT" to "Bảo vệ mắt (Vàng ấm)"
                    )
                    themes.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = key
                                    prefs.edit().putString("theme_mode", key).apply()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedTheme == key,
                                onClick = {
                                    selectedTheme = key
                                    prefs.edit().putString("theme_mode", key).apply()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Đóng", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}