package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class NotificationPayload(
    @SerializedName("title")
    var title: String = "",
    @SerializedName("body")
    var body: String = "",
    @SerializedName("click_action")
    val click_action: String = ".ui.post.PostActivity"
)