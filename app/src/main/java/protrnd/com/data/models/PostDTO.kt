package protrnd.com.data.models

data class PostDTO(
    val caption: String,
    val location: Location,
    val uploadurls: List<String>
)