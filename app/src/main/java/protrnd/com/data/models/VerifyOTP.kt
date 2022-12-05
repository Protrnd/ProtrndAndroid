package protrnd.com.data.models

data class VerifyOTP(
    var otpHash: String = "",
    var plainText: String = "",
    val registerDto: RegisterDTO? = RegisterDTO(),
    val type: String = "jwt"
)