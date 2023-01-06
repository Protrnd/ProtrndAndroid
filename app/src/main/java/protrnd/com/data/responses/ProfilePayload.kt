package protrnd.com.data.responses

import com.google.gson.annotations.SerializedName

data class ProfilePayload(
    @SerializedName("disabled")
    val disabled: Boolean,
    @SerializedName("email")
    val email: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("userName")
    val userName: Any
)