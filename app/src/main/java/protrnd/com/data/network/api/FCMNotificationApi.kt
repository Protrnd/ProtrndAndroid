package protrnd.com.data.network.api

import protrnd.com.data.models.FCMValues.Companion.CONTENT_TYPE
import protrnd.com.data.models.FCMValues.Companion.SERVER_KEY
import protrnd.com.data.models.PushNotification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMNotificationApi {
    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    fun sendNotification(@Body pushNotification: PushNotification): Call<PushNotification>
}