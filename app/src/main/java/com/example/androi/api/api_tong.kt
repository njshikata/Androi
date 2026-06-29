package com.example.androi.api

import com.example.androi.api.book_Home.Book_Detail_Api
import com.example.androi.api.book_Home.Book_Home_Api
import com.example.androi.api.caterory.CateroryAPI
import com.example.androi.api.chitietbook.BookReadAPI
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Nhớ import cái này

object api_tong {
    private const val BASE_URL = "http://163.61.183.247:8080/"

    // 1. Tạo OkHttpClient để tăng thời gian timeout lên 30 giây
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 2. Nhét OkHttpClient vào Retrofit
    val client: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gắn vào đây
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val cateroryApi: CateroryAPI by lazy {
        client.create(CateroryAPI::class.java)
    }

    val bookHomeApi: Book_Home_Api by lazy {
        client.create(Book_Home_Api::class.java)
    }
    val bookDetailApi: Book_Detail_Api by lazy {
        client.create(Book_Detail_Api::class.java)
    }
    // THÊM ĐOẠN NÀY VÀO: Khởi tạo BookReadAPI
    val bookReadApi: BookReadAPI by lazy {
        client.create(BookReadAPI::class.java)
    }
}