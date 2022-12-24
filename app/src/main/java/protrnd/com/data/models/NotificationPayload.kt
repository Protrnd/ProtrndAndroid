package protrnd.com.data.models

data class NotificationPayload(
    var title: String = "",
    var body: String = "",
    val click_action: String = ".ui.post.PostActivity"
)