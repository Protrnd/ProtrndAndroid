package protrnd.com.data.network

import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.data.responses.GetNotificationsResponseBody
import protrnd.com.data.responses.ProfileResponseBody
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApi {
    @GET("n/fetch/{page}")
    suspend fun getNotificationPaginated(@Path("page") page: Int): GetNotificationsResponseBody

    @GET("profile/{id}")
    suspend fun getProfileById(@Path("id") id: String): ProfileResponseBody

    @PUT("n/set/viewed/{id}")
    suspend fun setNotificationViewed(@Path("id") id: String): BasicResponseBody
}