package com.example.sitevent.Chat.Navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.sitevent.Chat.LCViewModel
import com.example.sitevent.Chat.Screens.ChatListScreen
import com.example.sitevent.Chat.Screens.LogInScreen
import com.example.sitevent.Chat.Screens.ProfileScreen
import com.example.sitevent.Chat.Screens.SignUpScreen
import com.example.sitevent.Chat.Screens.SingleChatScreen
import com.example.sitevent.Chat.Screens.StatusScreen

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    var vm = hiltViewModel<LCViewModel>()

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
        composable(DestinationScreen.Login.route) {
            LogInScreen(navController,vm)
        }

        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController,vm)
        }

        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController,vm)
        }

        composable(DestinationScreen.SingleChat.route) {
            val chatId = it.arguments?.getString("chatId")
            chatId?.let{
                SingleChatScreen(navController,vm,chatId)
            }
        }

        composable(DestinationScreen.StatusList.route) {
            StatusScreen(navController,vm)
        }

        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController,vm)
        }
    }
}
