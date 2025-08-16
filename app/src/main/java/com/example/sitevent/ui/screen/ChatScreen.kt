package com.example.sitevent.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sitevent.ui.Navigation.Screen
import com.example.sitevent.ui.viewModel.FcmViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: FcmViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    BottomBarScaffold(navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is chat screen")

            Button(onClick = {
                viewModel.sendCustomNotification(
                    title = "Profile Update",
                    body = "Please update your profile",
                    deepLinkRoute = Screen.EDIT_PROFILE_SCREEN.name
                )

                Toast.makeText(context, "Notification sent", Toast.LENGTH_SHORT).show()
            }) {
                Text("Send Profile Update Notification")
            }
        }
    }
}