package protrnd.com.data.responses

import protrnd.com.data.models.Notification

data class GetNotificationsResponseBody(
    val `data`: List<Notification>,
    val message: String,
    val statusCode: Int,
    val successful: Boolean
)