
package com.example.dattcnpm.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.dattcnpm.MainActivity
import com.example.dattcnpm.R
import com.example.dattcnpm.data.device.DeviceManager
import com.example.dattcnpm.data.remote.FirebaseProvider
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MMRAFirebaseMessagingService :
    FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(
            TAG,
            "Firebase vừa cấp hoặc làm mới FCM token."
        )

        val uid = FirebaseProvider.auth
            .currentUser?.uid ?: return

        serviceScope.launch {
            DeviceManager.syncCurrentDevice(
                context = applicationContext,
                expectedUid = uid,
                updatedToken = token
            )
        }
    }

    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Thông báo MMRA"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Bạn có thông báo mới."

        showNotification(title, body)
    }

    private fun showNotification(
        title: String,
        body: String
    ) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.w(
                    TAG,
                    "Chưa cấp quyền hiển thị thông báo."
                )
                return
            }
        }

        createNotificationChannel()

        val intent = Intent(
            this,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.take(100))
            .setContentText(body.take(200))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body.take(500))
            )
            .setPriority(
                NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(
                    (System.currentTimeMillis() %
                            Int.MAX_VALUE).toInt(),
                    notification
                )
        } catch (error: SecurityException) {
            Log.e(
                TAG,
                "Không được phép hiển thị thông báo.",
                error
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Thông báo MMRA",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Thông báo từ MMRA"
        }

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "MMRA_FCM"
        private const val CHANNEL_ID =
            "mmra_general_notifications"
    }
}
