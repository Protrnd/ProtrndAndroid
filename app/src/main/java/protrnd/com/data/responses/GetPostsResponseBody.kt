package protrnd.com.data.responses

import protrnd.com.data.models.Post

data class GetPostsResponseBody(
    val `data`: List<Post>,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)