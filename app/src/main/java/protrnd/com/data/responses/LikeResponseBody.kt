package protrnd.com.data.responses

data class LikeResponseBody(
    val `data`: Boolean,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)