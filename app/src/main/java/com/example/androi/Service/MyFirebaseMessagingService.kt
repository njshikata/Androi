package com.example.androi.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.androi.MainActivity
import com.example.androi.SQL.AppDatabase
import com.example.androi.SQL.enity.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Có chương mới!"
        val message = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Vào đọc ngay thôi."

        val bookIdString = remoteMessage.data["bookId"]
        val bookId = bookIdString?.toLongOrNull() ?: 0L

        // 👉 Truyền thêm bookId vào hàm
        showSystemNotification(title, message, bookId)

        if (bookId > 0) {
            val database = AppDatabase.getDatabase(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                database.notificationDao().insertNotification(
                    NotificationEntity(bookId = bookId, title = title, message = message)
                )
            }
        }
    }

    private fun showSystemNotification(title: String, message: String, bookId: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "book_updates_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Thông báo truyện mới", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // 👉 1. TẠO INTENT ĐỂ MỞ MAIN ACTIVITY VÀ TRUYỀN ID TRUYỆN
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("nav_to_book_id", bookId)
        }

        // 👉 2. TẠO PENDING INTENT
        val pendingIntent = PendingIntent.getActivity(
            this,
            bookId.toInt(), // Dùng bookId làm requestCode để phân biệt các thông báo
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // 👉 3. GẮN HÀNH ĐỘNG CLICK
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Firebase Token Mới: $token")
    }
}