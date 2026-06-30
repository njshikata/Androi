package com.example.androi.api.Login

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LoginAPI {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    // 👉 BỔ SUNG: Lấy thông tin user (để hiển thị tên thật bên Profile)
    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserResponse>
}