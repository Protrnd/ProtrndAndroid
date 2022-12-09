package protrnd.com.data.models

import java.io.Serializable

data class Post(
    val acceptgift: Boolean = false,
    val caption: String = "",
    val disabled: Boolean = false,
    val id: String = "",
    val identifier: String = "",
    val location: Location = Location(),
    val profileid: String = "",
    val time: String = "",
    val uploadurls: List<String> = arrayListOf()
) : Serializable