package protrnd.com.data.responses

import protrnd.com.data.models.Comment

data class GetCommentsResponseBody(
    val `data`: List<Comment>,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)