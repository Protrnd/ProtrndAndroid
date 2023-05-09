package protrnd.com.data.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import protrnd.com.R
import protrnd.com.data.models.Actions

class ApiRequestService : Service() {

    companion object {
        var action: (() -> Unit)? = null
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "API REQUEST CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
        val channel = NotificationChannel(
            notificationChannelId,
            "Api Request Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Api Request"
            it.lightColor = Color.RED
            it
        }
        notificationManager.createNotificationChannel(channel)

//        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
//            PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        }

        val builder: Notification.Builder = Notification.Builder(
            this,
            notificationChannelId
        )

        return builder
            .setContentTitle("Protrnd")
            .setContentText("Please wait...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val n = createNotification()
        if (intent != null && intent.action == Actions.START_FOREGROUND) {
            startForeground(1003, n)
            action.let {
                if (it != null)
                    it()
            }
        } else if (intent != null && intent.action == Actions.STOP_FOREGROUND) {
            stopForeground(true)
            stopSelfResult(1003)
        }
        return START_NOT_STICKY
    }
}