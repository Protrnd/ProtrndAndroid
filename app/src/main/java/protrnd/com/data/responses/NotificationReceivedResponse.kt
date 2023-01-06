package protrnd.com.data.responses

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import protrnd.com.R
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.post.PostActivity
import java.lang.Integer.MAX_VALUE
import java.util.*

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationReceivedResponse : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.notification != null) {
            receiveNotification(
                message.notification!!.title.toString(),
                message.notification!!.body.toString(),
                message.data["type"]!!,
                message.data["post_id"]!!
            )
        }
    }

    private fun receiveNotification(
        title: String,
        messageBody: String,
        action: String,
        id: String
    ) {
        val intent = if (action == "Post") {
            Intent(this, PostActivity::class.java).apply {
                putExtra("post_id", id)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        } else {
            Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        val requestCode = Date().time / 1000L % MAX_VALUE
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode.toInt() /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "Channel ID"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_received_ic)
            .setColor(Color.parseColor("#2D264B"))
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Protrnds Notifications Channel For Users",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            requestCode.toInt() /* ID of notification */,
            notificationBuilder.build()
        )
    }
}