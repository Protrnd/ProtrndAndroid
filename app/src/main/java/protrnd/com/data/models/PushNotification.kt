package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class PushNotification(
    @SerializedName("notification")
    val notification: NotificationPayload,
    @SerializedName("to")
    val to: String,
    @SerializedName("data")
    val data: NotificationData
)