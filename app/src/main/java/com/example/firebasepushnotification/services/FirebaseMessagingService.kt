package com.example.firebasepushnotification.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.firebasepushnotification.FirstActivity
import com.example.firebasepushnotification.MainActivity
import com.example.firebasepushnotification.R
import com.example.firebasepushnotification.SecondActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "my_fcm_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("ONDATA", "From: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: "Default Title"
        val body = remoteMessage.notification?.body ?: "Default Body"

        remoteMessage.data.isNotEmpty().let {
            val firstEntry = remoteMessage.data.entries.first()
            val pendingIntent: PendingIntent

            Log.d(TAG, "Message data key: " + firstEntry.key)
            Log.d(TAG, "Message data value: " + firstEntry.key)


            when (firstEntry.key) {
                firstEntry.key -> {
                    val intent = Intent(this, FirstActivity::class.java)
                    intent.putExtra("data_notification", firstEntry.value)
                    pendingIntent =
                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }
                firstEntry.key -> {
                    val intent = Intent(this, SecondActivity::class.java)
                    intent.putExtra("data_notification", firstEntry.value)
                    pendingIntent =
                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }
                else -> {
                    return
                }
            }


            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(
                        this@FirebaseMessagingService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(0, notificationBuilder)
            }

        }


//        remoteMessage.notification?.let
//        {
//            Log.d(TAG, "Message Notification Body: ${it.body}")
//            sendNotification(it.title ?: "Pesan Baru", it.body ?: "Anda memiliki pesan baru.")
//        }
    }

    override fun onDeletedMessages() {
        Log.d(TAG, "Received deleted messages notification")
    }

    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(
            this,
            MainActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan ikon notifikasi Anda
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioritas untuk notifikasi heads-up
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        // Sejak Android Oreo (API 26), channel notifikasi diperlukan.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName =
                getString(R.string.fcm_notification_channel_name) // Ambil dari strings.xml
            val channelDescription =
                getString(R.string.fcm_notification_channel_description) // Ambil dari strings.xml
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
                return
            }
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        Log.d(TAG, "Notification sent.")

    }
}