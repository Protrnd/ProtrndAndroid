package protrnd.com.data.responses

import protrnd.com.data.models.Comment

data class CommentResponseBody(
    val comment: Comment,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)