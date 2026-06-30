package com.example.androi.Review


data class ReviewRequest(
    val bookId: Long,
    // val userId: Long, -> XÓA DÒNG NÀY ĐI
    val rating: Int,
    val content: String
)