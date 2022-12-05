package protrnd.com.data.models

data class RegisterDTO(
    var accountType: String = "",
    var email: String = "",
    var fullName: String = "",
    var password: String = "",
    var userName: String = ""
)