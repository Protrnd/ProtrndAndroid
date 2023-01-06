package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class VerifyOTP(
    @SerializedName("otpHash")
    var otpHash: String = "",
    @SerializedName("plainText")
    var plainText: String = "",
    @SerializedName("registerDto")
    val registerDto: RegisterDTO? = RegisterDTO(),
    @SerializedName("type")
    val type: String = "jwt"
)