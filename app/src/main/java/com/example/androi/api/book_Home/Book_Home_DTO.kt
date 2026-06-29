package com.example.androi.api.book_Home

import com.google.gson.annotations.SerializedName

data class Book_Home_Dto(
    @SerializedName("id") val id: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("coverImageUrl") val coverImageUrl: String?,
    @SerializedName("contentType") val contentType: String?, // Enum hứng bằng String
    @SerializedName("status") val status: String?,           // Enum hứng bằng String
    @SerializedName("totalChapters") val totalChapters: Int?,
    @SerializedName("viewCount") val viewCount: Long?,
    @SerializedName("updatedAt") val updatedAt: String?      // Dùng String hứng LocalDateTime
)