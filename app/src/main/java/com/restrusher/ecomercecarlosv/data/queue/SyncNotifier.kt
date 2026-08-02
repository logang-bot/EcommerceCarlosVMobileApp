package com.restrusher.ecomercecarlosv.data.queue

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.restrusher.ecomercecarlosv.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.sync_channel_name))
            .setDescription(context.getString(R.string.sync_channel_description))
            .build()
        manager.createNotificationChannel(channel)
    }

    fun notifyStarted() = show(
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_notification)
            .setContentTitle(context.getString(R.string.sync_notification_started_title))
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()
    )

    fun notifySuccess() = show(
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_notification)
            .setContentTitle(context.getString(R.string.sync_notification_success_title))
            .setAutoCancel(true)
            .build()
    )

    fun notifyFailure() = show(
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_notification)
            .setContentTitle(context.getString(R.string.sync_notification_failure_title))
            .setContentText(context.getString(R.string.sync_notification_failure_body))
            .setAutoCancel(true)
            .build()
    )

    /**
     * Clears the notification without reporting an outcome. [notifyStarted] posts an ongoing
     * notification the user cannot swipe away, so any path that ends without success or failure —
     * a flush deferred for want of a session — must call this or it stays on screen forever.
     */
    fun dismiss() = manager.cancel(NOTIFICATION_ID)

    // Silently skips if the user hasn't granted POST_NOTIFICATIONS (Android 13+)
    // or has disabled notifications for the app in system settings.
    @SuppressLint("MissingPermission")
    private fun show(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }
        if (!manager.areNotificationsEnabled()) return
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
