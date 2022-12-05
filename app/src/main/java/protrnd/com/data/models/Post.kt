package protrnd.com.data.models

import java.io.Serializable

data class Post(
    val acceptgift: Boolean,
    val caption: String,
    val disabled: Boolean,
    val id: String,
    val identifier: String,
    val location: Location,
    val profileid: String,
    val time: String,
    val uploadurls: List<String>
) : Serializable