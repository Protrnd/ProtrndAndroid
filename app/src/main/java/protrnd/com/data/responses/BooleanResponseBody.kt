package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName

data class BooleanResponseBody(
    @SerializedName("data")
    val `data`: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)