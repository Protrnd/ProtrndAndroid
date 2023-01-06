package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class ProfileDTO(
    @SerializedName("accountType")
    val accountType: String,
    @SerializedName("backgroundImageUrl")
    val backgroundImageUrl: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("profileImage")
    val profileImage: String,
    @SerializedName("userName")
    val userName: String
)