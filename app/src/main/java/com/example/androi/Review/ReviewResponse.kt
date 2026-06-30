package com.example.androi.Review

data class ReviewResponse(
    val id: Long,
    val username: String,
    val rating: Int,
    val content: String,
    val createdAt: String // Spring Boot trả về dạng Chuỗi ISO (VD: 2026-05-10T10:15:30)
)