package com.example.androi.api.chitietbook

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface BookReadAPI {

    // 1. Lấy danh sách các chương của một truyện (dùng cho màn hình chi tiết truyện)
    @GET("api/books/{bookId}/chapters")
    fun getChapters(@Path("bookId") bookId: Long): Call<List<ChapterSummary>>

    // 2. Lấy nội dung chi tiết của một chương (dùng cho màn hình đọc truyện)
    @GET("api/books/chapters/{chapterId}/read")
    fun readChapter(@Path("chapterId") chapterId: Long): Call<ChapterContent>

    // Lưu ý: Nếu bạn đang dùng Coroutine (ViewModel), hãy thêm chữ `suspend`
    // ở đầu hàm và bọc kiểu trả về bằng `Response<T>`. Ví dụ:
    // suspend fun readChapter(@Path("chapterId") chapterId: Long): Response<ChapterContent>
}