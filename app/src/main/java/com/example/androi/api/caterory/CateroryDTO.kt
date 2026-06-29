package com.example.androi.api.caterory

import com.google.gson.annotations.SerializedName

data class CateroryDTO(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("iconUrl") val iconUrl: String?,
    @SerializedName("targetContentType") val targetContentType: String?, // Dùng String để hứng Enum từ Java
    @SerializedName("totalBooks") val totalBooks: Int?
)