package com.example.androi.screen.HomePage.Home.Book_Doc

import android.content.Context
import android.widget.ImageView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.Coil
import coil.request.ImageRequest
import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.ReadingEntity
import com.example.androi.api.api_tong
import com.example.androi.api.chitietbook.ChapterContent
import com.example.androi.api.chitietbook.ChapterSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReadScreen(
    navController: NavController,
    bookId: Long,
    initialChapterId: Long,
    isOffline: Boolean = false
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val readingDao = remember { database.readingDao() }
    val chapterDao = remember { database.chapterDao() }

    var currentChapterId by remember { mutableStateOf(initialChapterId) }

    // --- CÁC STATE BẮT BUỘC ---
    var chapterTitle by remember { mutableStateOf("Đang tải...") }
    var contentType by remember { mutableStateOf("NOVEL") }
    var novelText by remember { mutableStateOf<String?>(null) }
    var comicUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var chapterList by remember { mutableStateOf<List<ChapterSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(false) }
    var isUiVisible by remember { mutableStateOf(true) }

    // 👉 STATE CHO MENU VÀ THEME
    var showMoreMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var selectedTheme by remember { mutableStateOf(prefs.getString("theme_mode", "LIGHT") ?: "LIGHT") }

    // State cho vị trí cuộn
    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope() // Gắn với vòng đời của Composable này

    // 👉 1. LƯU VỊ TRÍ KHI THOÁT HOẶC ĐỔI CHƯƠNG
    DisposableEffect(currentChapterId) {
        onDispose {
            // Lấy vị trí an toàn, tránh gọi hàm List khi danh sách rỗng
            val currentPos = if (contentType == "NOVEL") {
                scrollState.value
            } else {
                if (comicUrls.isNotEmpty()) listState.firstVisibleItemIndex else 0
            }

            // Chạy Scope độc lập IO để đảm bảo dữ liệu được lưu xuống DB
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                readingDao.saveHistory(ReadingEntity(bookId, currentChapterId, chapterTitle, currentPos))
            }
        }
    }

    // 2. TẢI DANH SÁCH MỤC LỤC
    LaunchedEffect(bookId) {
        if (isOffline) {
            val localChapters = chapterDao.getChaptersByBook(bookId)
            chapterList = localChapters.mapIndexed { index, it ->
                ChapterSummary(id = it.chapterId, title = it.title, isPremium = false, chapterIndex = index.toDouble())
            }
        } else {
            api_tong.bookReadApi.getChapters(bookId).enqueue(object : Callback<List<ChapterSummary>> {
                override fun onResponse(call: Call<List<ChapterSummary>>, response: Response<List<ChapterSummary>>) {
                    if (response.isSuccessful && response.body() != null) chapterList = response.body()!!
                }
                override fun onFailure(call: Call<List<ChapterSummary>>, t: Throwable) {}
            })
        }
    }

    // 👉 3. TẢI NỘI DUNG CHI TIẾT & CUỘN AN TOÀN
    LaunchedEffect(currentChapterId) {
        isLoading = true
        errorMessage = null

        try {
            // Lấy lịch sử xem dở để biết vị trí cuộn
            val history = readingDao.getHistoryByBook(bookId)
            val isContinuing = history != null && history.lastChapterId == currentChapterId

            if (isOffline) {
                val localData = chapterDao.getChapterById(currentChapterId)
                if (localData != null) {
                    chapterTitle = localData.title
                    contentType = localData.contentType
                    if (localData.contentType == "NOVEL") {
                        novelText = localData.textContent ?: "Chưa có nội dung."
                        comicUrls = emptyList() // Xoá sạch list cũ
                    } else {
                        comicUrls = localData.localImagePaths?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                        novelText = null // Xoá sạch text cũ
                    }
                    isLoading = false

                    // Cuộn offline an toàn
                    scope.launch {
                        delay(150)
                        try {
                            if (isContinuing && history != null) {
                                if (contentType == "NOVEL") scrollState.scrollTo(history.lastScrollPosition)
                                else if (comicUrls.isNotEmpty() && history.lastScrollPosition < comicUrls.size) listState.scrollToItem(history.lastScrollPosition)
                            } else {
                                if (contentType == "NOVEL") scrollState.scrollTo(0)
                                else if (comicUrls.isNotEmpty()) listState.scrollToItem(0)
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } else {
                    errorMessage = "Chương này chưa được tải về."
                    isLoading = false
                }
            } else {
                // TẢI TỪ MẠNG
                api_tong.bookReadApi.readChapter(currentChapterId).enqueue(object : Callback<ChapterContent> {
                    override fun onResponse(call: Call<ChapterContent>, response: Response<ChapterContent>) {
                        if (response.isSuccessful && response.body() != null) {
                            val content = response.body()!!
                            chapterTitle = content.title ?: "Không có tiêu đề"
                            contentType = content.contentType ?: "NOVEL"

                            // Reset đúng kiểu content để giao diện vẽ chuẩn xác
                            if (contentType == "NOVEL") {
                                novelText = content.textContent
                                comicUrls = emptyList()
                            } else {
                                comicUrls = content.imageUrls ?: emptyList()
                                novelText = null
                            }

                            isLoading = false // Phải set false ở đây để Compose Render list ra ngoài

                            // Phục hồi vị trí cuộn online (An toàn tuyệt đối)
                            scope.launch {
                                delay(200) // Đợi 200ms để Compose kịp vẽ xong View
                                try {
                                    if (isContinuing && history != null) {
                                        if (contentType == "NOVEL") {
                                            scrollState.scrollTo(history.lastScrollPosition)
                                        } else if (comicUrls.isNotEmpty() && history.lastScrollPosition < comicUrls.size) {
                                            listState.scrollToItem(history.lastScrollPosition)
                                        }
                                    } else {
                                        // Chương mới chưa đọc -> Về đầu chương
                                        if (contentType == "NOVEL") {
                                            scrollState.scrollTo(0)
                                        } else if (comicUrls.isNotEmpty()) {
                                            listState.scrollToItem(0)
                                        }
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        } else {
                            errorMessage = "Lỗi tải: ${response.code()}"
                            isLoading = false
                        }
                    }
                    override fun onFailure(call: Call<ChapterContent>, t: Throwable) {
                        errorMessage = "Lỗi mạng: ${t.message}"
                        isLoading = false
                    }
                })
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi dữ liệu: ${e.message}"
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) { detectTapGestures(onTap = { isUiVisible = !isUiVisible }) }
    ) {
        // --- NỘI DUNG CHÍNH (TRUYỆN CHỮ HOẶC TRANH) ---
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                else -> {
                    if (contentType == "NOVEL") {
                        NovelContentView(text = novelText ?: "Đang cập nhật...", scrollState = scrollState)
                    } else if (contentType == "COMIC") {
                        ComicContentView(imageUrls = comicUrls, listState = listState)
                    }
                }
            }
        }

        // --- THANH ĐIỀU HƯỚNG BÊN TRÊN CẢI TIẾN ---
        AnimatedVisibility(
            visible = isUiVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 👉 Thêm weight(1f) để Text đẩy nút 3 chấm sang sát mép phải
                Text(
                    text = chapterTitle,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 👉 NÚT MENU 3 CHẤM
                Box {
                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Thêm")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cài đặt giao diện") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                showThemeDialog = true // Mở bảng chọn Theme
                            }
                        )
                    }
                }
            }
        }

        // --- NÚT MỞ MỤC LỤC GÓC DƯỚI ---
        AnimatedVisibility(
            visible = isUiVisible,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 24.dp)
        ) {
            FloatingActionButton(
                onClick = { showBottomSheet = true; isUiVisible = false },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = "Mục lục")
            }
        }
    }

    // 👉 4. BOTTOM SHEET (DANH SÁCH CHƯƠNG HOÀN CHỈNH)
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Mục lục",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chapterList) { chapter ->
                        val isSelected = chapter.id == currentChapterId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    currentChapterId = chapter.id // Cập nhật ID để tự động load chương mới
                                    showBottomSheet = false // Đóng mục lục
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chapter.title,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (chapter.isPremium) {
                                Text("VIP", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 👉 5. BẢNG DIALOG CÀI ĐẶT GIAO DIỆN (Giống hệt ở Narbar)
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cài đặt giao diện", fontWeight = FontWeight.Bold) },
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
                            Text(text = label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- CÁC HÀM PHỤ TRỢ (NỘI DUNG) ---
@Composable
fun NovelContentView(text: String, scrollState: ScrollState) {
    SelectionContainer {
        Text(
            text = text,
            fontSize = 19.sp,
            lineHeight = 34.sp,
            textAlign = TextAlign.Justify,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 24.dp)
        )
    }
}

@Composable
fun ComicContentView(imageUrls: List<String>, listState: LazyListState) {
    if (imageUrls.isEmpty()) return
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(imageUrls) { url ->
            AndroidView(
                factory = { context ->
                    ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_XY }
                },
                update = { imageView ->
                    val request = ImageRequest.Builder(imageView.context)
                        .data(url)
                        .target(imageView)
                        .build()
                    Coil.imageLoader(imageView.context).enqueue(request)
                }
            )
        }
    }
}