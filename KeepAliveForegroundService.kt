package com.msgsafe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.msgsafe.app.R

class KeepAliveForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "msgsafe_keepalive"
        private const val NOTIF_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveForegroundService::class.java)
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MsgSafe is watching")
            .setContentText("Catching deleted messages in the background")
            .setSmallIcon(R.drawable.ic_shield) // add a simple vector icon with this name
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MsgSafe Background Service",
                NotificationManager.IMPORTANCE_MIN
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
