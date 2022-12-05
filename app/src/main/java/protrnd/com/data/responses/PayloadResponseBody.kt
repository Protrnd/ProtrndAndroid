package protrnd.com.data.responses

data class PayloadResponseBody(
    val `data`: ProfilePayload,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)