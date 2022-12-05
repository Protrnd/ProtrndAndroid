package protrnd.com.data.responses

import protrnd.com.data.models.Location

data class GetLocationResponseBody(
    val `data`: List<Location>,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)