package protrnd.com.data.responses

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import protrnd.com.R
import protrnd.com.data.models.ReceiveNotification
import protrnd.com.ui.post.PostActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessageClass: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null) {
            val notification = Gson().fromJson(message.notification!!.body, ReceiveNotification::class.java)
            receiveNotification(
                message.notification!!.title.toString(),
                notification.body,
                notification.type,
                notification.id
            )
        }
        super.onMessageReceived(message)
    }

    private fun receiveNotification(title: String, messageBody: String, action: String, id: String) {
        val intent = Intent(this, PostActivity::class.java)
        intent.putExtra("post_id",id)
        val pendingIntent = PendingIntent.getActivity(this, 5050 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val channelId = "Channel ID"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.launcher_filled_ic)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Protrnd Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(5050 /* ID of notification */, notificationBuilder.build())
    }
}