package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class NotificationData(
    @SerializedName("username")
    val username: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("post_id")
    val post_id: String,
    @SerializedName("body")
    val body: String,
)