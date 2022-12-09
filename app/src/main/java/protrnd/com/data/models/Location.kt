package protrnd.com.data.models

import java.util.*

data class Location(
    val cities: List<String> = arrayListOf(),
    val id: String = UUID.randomUUID().toString(),
    val state: String = ""
)