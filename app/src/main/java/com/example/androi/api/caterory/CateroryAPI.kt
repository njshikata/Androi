package com.example.androi.api.caterory

import retrofit2.http.GET
import retrofit2.http.Query

interface CateroryAPI {
    // Gọi đến GET /api/categories
    @GET("api/categories")
    suspend fun getCategories(
        // @Query giúp hỗ trợ bộ lọc tự động: vd ?type=COMIC. Mặc định là null nếu không lọc.
        @Query("type") type: String? = null
    ): List<CateroryDTO>
}