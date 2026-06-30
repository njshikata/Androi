package com.example.androi.api.book_Home // Đổi lại package theo đúng máy bạn nhé

import com.google.gson.annotations.SerializedName

data class Book_Detail_Dto(
    @SerializedName("id") val id: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("coverImageUrl") val coverImageUrl: String?,
    @SerializedName("contentType") val contentType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("totalChapters") val totalChapters: Int?,
    @SerializedName("viewCount") val viewCount: Long?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("categories") val categories: List<String>?,

    // THÊM 2 DÒNG NÀY ĐỂ HỨNG DỮ LIỆU TỪ BACKEND
    @SerializedName("averageRating") val averageRating: Double?,
    @SerializedName("totalReviews") val totalReviews: Long?
)