package com.example.sitevent

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.sitevent.Notification.AddScheduleScreen
import com.example.sitevent.ui.Navigation.AppNavigation
import com.example.sitevent.ui.theme.SitEventTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sitevent.data.ThemePreferenceManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {

//    FirebaseMessaging.getInstance().token
//        .addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM", "Fetching FCM token failed", task.exception)
//                return@addOnCompleteListener
//            }
//
//            val token = task.result
//            Log.d("FCM", "FCM token (manual fetch): $token")
//        }


    super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferenceManager = ThemePreferenceManager(this)
            val themePref by themePreferenceManager.themeFlow.collectAsState(initial = "System")
            val darkTheme = when (themePref) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            SitEventTheme(darkTheme = darkTheme) {
                AppNavigation()
//                AddScheduleScreen()


//           NoteScreen()
//                ChatAppNavigation()
            }
        }
    }
}
