package com.example.androi.api.book_Home

import retrofit2.http.GET
import retrofit2.http.Path

interface Book_Home_Api {

    // API: Top 10 Truyện tranh nổi bật nhất
    @GET("api/books/trending/comics")
    suspend fun getTrendingComics(): List<Book_Home_Dto>

    // API: Top 10 Tiểu thuyết nổi bật nhất
    @GET("api/books/trending/novels")
    suspend fun getTrendingNovels(): List<Book_Home_Dto>

    // API: Top 10 Bộ mới ra mắt
    @GET("api/books/newest")
    suspend fun getNewestBooks(): List<Book_Home_Dto>

    // API: Top 10 Bộ vừa cập nhật chap mới
    @GET("api/books/recently-updated")
    suspend fun getRecentlyUpdatedBooks(): List<Book_Home_Dto>

    // API: Xem toàn bộ truyện theo Thể loại (Category)
    @GET("api/books/category/{categoryId}")
    suspend fun getBooksByCategory(
        @Path("categoryId") categoryId: Int
    ): List<Book_Home_Dto>
}