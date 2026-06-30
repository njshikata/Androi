package com.example.androi.Review

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewAPI {
    // API Lấy danh sách đánh giá của truyện (Không cần token, ai xem cũng được)
    @GET("api/reviews/book/{bookId}")
    suspend fun getReviewsByBook(@Path("bookId") bookId: Long): Response<List<ReviewResponse>>

    // 👉 BỔ SUNG: Thêm Header truyền Token để Backend biết ai đang Review
    @POST("api/reviews")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequest
    ): Response<ReviewResponse>
}