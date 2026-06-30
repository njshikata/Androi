package com.example.androi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.androi.screen.AppNavigation
import com.example.androi.ui.theme.AndroiTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay

@Composable
private fun rememberNetworkConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = true) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        value = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { value = true }
            override fun onLost(network: Network) { value = false }
        }
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 👉 ĐỌC ID TRUYỆN TỪ THÔNG BÁO (Nếu không có thì mặc định là -1)
        val bookIdFromNotification = intent.getLongExtra("nav_to_book_id", -1L)

        setContent {
            AndroiTheme {
                var showSplash by remember { mutableStateOf(true) }
                FirebaseApp.initializeApp(this)
                val isOnline by rememberNetworkConnectivityState()

                // 👉 GỐC LUÔN LÀ HOME HOẶC DOWNLOAD
                val startRoute = if (isOnline) "home" else "download_page"

                LaunchedEffect(Unit) {
                    delay(5000)
                    showSplash = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen(isOnline = isOnline)
                    } else {
                        // 👉 TRUYỀN ID TRUYỆN VÀO APPS NAVIGATION
                        AppNavigation(
                            startDestination = startRoute,
                            targetBookId = bookIdFromNotification
                        )
                    }
                }
            }
        }
    }
}