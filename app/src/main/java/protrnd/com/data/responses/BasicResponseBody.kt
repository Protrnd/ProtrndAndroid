package protrnd.com.data.responses

data class BasicResponseBody(
    val `data`: Any,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)