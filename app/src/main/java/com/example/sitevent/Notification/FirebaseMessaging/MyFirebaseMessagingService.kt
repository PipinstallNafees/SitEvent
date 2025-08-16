package com.example.sitevent.Notification.FirebaseMessaging

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.sitevent.MainActivity
import com.example.sitevent.R
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import javax.inject.Inject
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        firebaseMessaging.subscribeToTopic("chat")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic: chat")
                } else {
                    Log.e("FCM", "Subscription failed", task.exception)
                }
            }
    }

    // MyFirebaseMessagingService.kt
    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val payload = remoteMessage.data
        val fcmNotification = remoteMessage.notification

        // Extract values safely
        val deepLinkRoute = payload["deepLink"] ?: ""
        val senderName = payload["senderName"] ?: ""

        // ✅ Build intent for deep link navigation
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "sitevent://app/$deepLinkRoute".toUri() // Android Intent.data (Uri)
            putExtra("senderName", senderName) // pass extra info
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Notification channel ID (must be created beforehand)
        val channelId = "default_channel"

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.google_icon) // make sure this icon exists
            .setContentTitle(fcmNotification?.title ?: "New Notification")
            .setContentText(fcmNotification?.body ?: "You have a new message")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Optional: add an action button
        notificationBuilder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.google_icon,
                "Open",
                pendingIntent
            ).build()
        )

        // ✅ Unique ID so multiple notifications don’t overwrite each other
        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

