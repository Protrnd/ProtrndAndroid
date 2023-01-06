package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Profile

data class ProfileListResponseBody(
    @SerializedName("data")
    val `data`: List<Profile>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
)