package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Comment

data class GetCommentsResponseBody(
    @SerializedName("data")
    val `data`: List<Comment>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)