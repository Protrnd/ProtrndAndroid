package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName

data class BasicResponseBody(
    @SerializedName("data")
    val `data`: Any,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)