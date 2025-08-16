package com.example.sitevent.Notification.FirebaseMessaging

data class ChatState(
    val isEnteringToken: Boolean = true,
    val remoteToken: String = "",
    val messageText: String = ""
)