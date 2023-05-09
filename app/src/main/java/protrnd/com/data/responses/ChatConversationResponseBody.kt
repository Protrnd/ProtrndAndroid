package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Chat

data class ChatConversationResponseBody(
    @SerializedName("data")
    val `data`: List<Chat>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)
