package com.example.androi.api.Login

import com.google.gson.annotations.SerializedName

/**
 * Data class gửi lên server (tương ứng với LoginDTO bên Spring Boot)
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class nhận về từ server (tương ứng với Map trả về trong Controller)
 */
data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String
)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)
// 👉 BỔ SUNG: Model đón dữ liệu từ /me
data class UserResponse(
    val username: String,
    val email: String,
    val role: String
)