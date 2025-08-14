package com.example.sitevent.Chat
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import javax.inject.Inject
//import javax.inject.Singleton
//import retrofit2.Response
//
//
//@Singleton
//class MessagingRepository @Inject constructor(
//    private val context: Context,
//    private val db: FirebaseFirestore, // Inject Firestore
//    private val notificationService: NotificationService // Inject the service
//) {
//
//    suspend fun sendNotification(userIds: List<String>, title: String, body: String): Response<Unit> {
//        val notificationData = NotificationData(userIds, title, body)
//        return notificationService.sendNotification(notificationData)
//    }
//
//    fun updateTokenInFirestore(token: String): Task<Void> {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        return if (userId != null) {
//            db.collection("users").document(userId).update("fcmToken", token)
//        } else {
//            Tasks.forException(Exception("User not logged in"))
//        }
//    }
//
//    fun showNotification(title: String?, body: String?) {
//        val channelId = "default_channel"
//        val channelName = "Default Channel"
//
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setSmallIcon(R.drawable.google_icon)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(0, notification)
//    }
//}
