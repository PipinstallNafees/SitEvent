package com.example.sitevent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sitevent.ui.Navigation.AppNavigation
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.Navigation.Screen
import com.example.sitevent.ui.theme.SitEventTheme
import com.example.sitevent.ui.viewModel.FcmViewModel
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

    private val fcmViewModel: FcmViewModel by viewModels()
    private var navController: NavHostController? = null

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()

        // ✅ Fetch FCM token (for logging or sending to backend)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            Log.d("FCM", "FCM token: ${task.result}")
        }

        setContent {

            SitEventTheme {
                val controller = rememberNavController()
                navController = controller

            val themePreferenceManager = ThemePreferenceManager(this)
            val themePref by themePreferenceManager.themeFlow.collectAsState(initial = "System")
            val darkTheme = when (themePref) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
           


                // ✅ Handle deep link if app opened via notification
                LaunchedEffect(Unit) {
                    intent?.data?.let { uri ->
                        handleDeepLink(uri, controller)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(navController = controller)
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // ✅ Handle deep link when activity already running
        intent.data?.let { uri ->
            navController?.let { handleDeepLink(uri, it) }
        }
    }

    /**
     * Deep link handler:
     * Parses the URI into route + arguments
     */
    private fun handleDeepLink(uri: Uri, navController: NavHostController) {
        Log.d("DEEP_LINK", "Handling deep link: $uri")

        val segments = uri.pathSegments
        if (segments.isEmpty()) {
            Log.d("DEEP_LINK", "No path segments found")
            return
        }

        val route = segments.first()
        val args = segments.drop(1)

        Log.d("DEEP_LINK", "Route: $route, Args: $args")

        when (route) {
            // Direct screens
            Screen.EDIT_PROFILE_SCREEN.name -> {
                navController.navigate(Screen.EDIT_PROFILE_SCREEN.name)
            }

            // Screens with arguments
            Screen.SINGLE_CATEGORY_SCREEN.name -> if (args.size >= 1) {
                navController.navigate(NavigationItem.SingleCategory.createRoute(args[0]))
            }

            Screen.CLUB_DETAIL_SCREEN.name -> if (args.size >= 2) {
                navController.navigate(NavigationItem.ClubDetail.createRoute(args[0], args[1]))
            }

            Screen.EVENT_DETAIL_SCREEN.name -> if (args.size >= 3) {
                navController.navigate(NavigationItem.EventDetail.createRoute(args[0], args[1], args[2]))
            }

            Screen.USER_TICKET_DETAIL_SCREEN.name -> if (args.size >= 2) {
                navController.navigate(NavigationItem.UserTicketDetail.createRoute(args[0], args[1]))
            }

            // Default simple route
            else -> {
                try {
                    Screen.valueOf(route).let {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("DEEP_LINK", "Invalid route: $route")
                }
            }
        }
    }

    /**
     * Request notification permission (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}
