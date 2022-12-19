package protrnd.com.data.models

data class ReceiveNotification(
    val username: String,
    val type: String,
    val id: String,
    val body: String
)