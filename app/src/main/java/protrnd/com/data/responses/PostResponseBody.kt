package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Post

data class PostResponseBody(
    @SerializedName("data")
    val `data`: Post,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)