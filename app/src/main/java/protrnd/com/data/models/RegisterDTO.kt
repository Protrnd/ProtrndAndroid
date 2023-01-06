package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class RegisterDTO(
    @SerializedName("accountType")
    var accountType: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("fullName")
    var fullName: String = "",
    @SerializedName("password")
    var password: String = "",
    @SerializedName("userName")
    var userName: String = ""
)