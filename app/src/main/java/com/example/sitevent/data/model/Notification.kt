package com.example.sitevent.data.model

data class Notification(
    val notificationId: String = System.currentTimeMillis().toString(),
    val userId: String,
    val title: String,
    val body: String,
    val seen: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val targetId: String? = null,
    val type: String = "GENERAL"
)

enum class NotificationType {
    GENERAL, CLUB, EVENT, TICKET, CHAT
}