package protrnd.com.data.responses

import protrnd.com.data.models.Profile

data class ProfileListResponseBody(
    val `data`: List<Profile>,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)