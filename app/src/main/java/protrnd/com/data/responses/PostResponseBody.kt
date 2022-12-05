package protrnd.com.data.responses

import protrnd.com.data.models.Post

data class PostResponseBody(
    val `data`: Post,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)