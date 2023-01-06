package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class PostDTO(
    @SerializedName("caption")
    val caption: String,
    @SerializedName("location")
    val location: Location,
    @SerializedName("uploadurls")
    val uploadurls: List<String>
)