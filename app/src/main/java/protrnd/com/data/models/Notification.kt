package protrnd.com.data.models

data class Notification(
    val id: String,
    val identifier: String,
    val item_id: String,
    val message: String,
    val receiverid: String,
    val senderid: String,
    val time: String,
    val type: String,
    val viewed: Boolean
)