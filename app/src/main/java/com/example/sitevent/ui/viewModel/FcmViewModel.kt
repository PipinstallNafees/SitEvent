package com.example.sitevent.ui.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitevent.Notification.FirebaseMessaging.ChatState
import com.example.sitevent.data.model.NotificationBody
import com.example.sitevent.data.model.SendMessageDto
import com.example.sitevent.data.repository.Inteface.FcmRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class FcmViewModel @Inject constructor(
    private val repository: FcmRepository
) : ViewModel() {

    var state by mutableStateOf(ChatState())
        private set

    fun sendMessage(isBroadcast: Boolean) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = if (isBroadcast) null else state.remoteToken,
                notification = NotificationBody(
                    title = "New message!",
                    body = state.messageText
                ),
                data = emptyMap()
            )

            try {
                if (isBroadcast) {
                    repository.broadcast(messageDto)
                } else {
                    repository.sendMessage(messageDto)
                }
                state = state.copy(messageText = "")
            } catch (e: HttpException) {
                Log.e("FCM", "HTTP error: ${e.response()?.errorBody()?.string()}")
            } catch (e: IOException) {
                Log.e("FCM", "Network error: ${e.message}")
            }
        }
    }

    fun sendCustomNotification(
        title: String,
        body: String,
        deepLinkRoute: String,
        senderName: String = "System",
        targetToken: String? = null
    ) {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                to = targetToken,
                notification = NotificationBody(title, body),
                data = mapOf(
                    "deepLink" to deepLinkRoute,
                    "senderName" to senderName
                )
            )

            try {
                if (targetToken == null) {
                    repository.broadcast(messageDto)
                } else {
                    repository.sendMessage(messageDto)
                }
            } catch (e: HttpException) {
                Log.e("FCM", "Custom notification HTTP error: ${e.response()?.errorBody()?.string()}")
            } catch (e: IOException) {
                Log.e("FCM", "Custom notification network error: ${e.message}")
            }
        }
    }
}
