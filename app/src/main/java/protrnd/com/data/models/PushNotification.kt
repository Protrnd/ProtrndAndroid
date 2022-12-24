package protrnd.com.data.models

data class PushNotification(
    val notification: NotificationPayload,
    val to: String,
    val data: NotificationData
)