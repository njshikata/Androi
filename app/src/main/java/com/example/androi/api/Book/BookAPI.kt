package com.example.androi.api.book_Home

import retrofit2.http.GET
import retrofit2.http.Path

interface Book_Detail_Api {
    @GET("api/book-details/{bookId}")
    suspend fun getBookDetail(
        @Path("bookId") bookId: Long
    ): Book_Detail_Dto
}