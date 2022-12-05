package protrnd.com.data.responses

import protrnd.com.data.models.Profile

data class ProfileResponseBody (
    val `data`: Profile,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)