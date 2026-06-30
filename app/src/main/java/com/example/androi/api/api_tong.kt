package com.example.androi.api

import android.content.Context
import com.example.androi.Review.ReviewAPI
import com.example.androi.Security.AuthInterceptor
import com.example.androi.api.Login.LoginAPI
import com.example.androi.api.book_Home.Book_Detail_Api
import com.example.androi.api.book_Home.Book_Home_Api
import com.example.androi.api.caterory.CateroryAPI
import com.example.androi.api.chitietbook.BookReadAPI
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object api_tong {
    private const val BASE_URL = "http://163.61.183.247:8080/"

    // 1. Tạo CookieJar để tự động hứng và gửi Cookie (cần cho Fingerprint của Spring Boot)
    private val cookieJar = object : CookieJar {
        private val cookieStore = HashMap<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies.toMutableList()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: ArrayList()
        }
    }

    // Biến lưu trữ Retrofit
    private var retrofit: Retrofit? = null

    // 2. Viết hàm lấy Retrofit, nhận vào Context
    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context)) // <--- GẮN INTERCEPTOR Ở ĐÂY
                .cookieJar(cookieJar)                     // <--- GẮN COOKIE JAR Ở ĐÂY
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    // 3. Khai báo các API (Lưu ý: Giờ chúng ta biến nó thành hàm nhận Context)
    fun getLoginApi(context: Context): LoginAPI = getClient(context).create(LoginAPI::class.java)

    fun getCateroryApi(context: Context): CateroryAPI = getClient(context).create(CateroryAPI::class.java)

    fun getBookHomeApi(context: Context): Book_Home_Api = getClient(context).create(Book_Home_Api::class.java)

    fun getBookDetailApi(context: Context): Book_Detail_Api = getClient(context).create(Book_Detail_Api::class.java)

    fun getBookReadApi(context: Context): BookReadAPI = getClient(context).create(BookReadAPI::class.java)

    fun getReviewApi(context: Context): ReviewAPI = getClient(context).create(ReviewAPI::class.java)
}