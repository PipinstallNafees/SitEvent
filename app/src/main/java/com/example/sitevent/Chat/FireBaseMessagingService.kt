package com.example.sitevent.Chat
//
//import android.util.Log
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import dagger.hilt.android.HiltAndroidApp
//import javax.inject.Inject
//
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//    @Inject
//    //lateinit var messagingRepository: MessagingRepository
//
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        messagingRepository.updateTokenInFirestore(token)
//            .addOnSuccessListener {
//                Log.d("FCM", "Token successfully updated.")
//            }
//            .addOnFailureListener {
//                Log.e("FCM", "Token update failed.", it)
//            }
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        remoteMessage.data.let {
//            messagingRepository.showNotification(it["title"], it["body"]) // Delegate logic
//        }
//    }
//}
