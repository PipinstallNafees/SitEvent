package com.example.sitevent.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.sitevent.Notification.Constants.RMNDR_NOTI_TITLE_KEY

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scheduleNotificationService = context?.let { ReminderNotification(it) }
        val title: String? = intent?.getStringExtra(RMNDR_NOTI_TITLE_KEY)
        scheduleNotificationService?.sendReminderNotification(title)
    }
}