package com.example.androi.api.chitietbook

import com.google.gson.annotations.SerializedName

// Tương ứng với User_Book_DTO.ChapterSummary
data class ChapterSummary(
    @SerializedName("id") val id: Long,
    @SerializedName("chapterIndex") val chapterIndex: Double, // Dùng Double để map với BigDecimal
    @SerializedName("title") val title: String,
    @SerializedName("premium") val isPremium: Boolean
)

// Tương ứng với User_Book_DTO.ChapterContent
data class ChapterContent(
    @SerializedName("chapterId") val chapterId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("contentType") val contentType: String, // Trả về "NOVEL" hoặc "COMIC"
    @SerializedName("textContent") val textContent: String?, // Có thể null nếu là truyện tranh
    @SerializedName("imageUrls") val imageUrls: List<String>? // Có thể null nếu là truyện chữ
)