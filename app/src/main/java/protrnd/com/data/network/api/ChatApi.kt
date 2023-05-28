package protrnd.com.data.network.api

import protrnd.com.data.models.ChatDTO
import protrnd.com.data.responses.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {
    @POST("chat/send")
    fun sendChat(@Body chat: ChatDTO): Call<BooleanResponseBody>

    @GET("chat/conversations")
    fun getConversations(): Call<ConversationsResponseBody>

    @GET("chat/{id}")
    fun getChatData(@Path("id") id: String): Call<ChatConversationResponseBody>

    @GET("chat/conversation/{id}")
    fun getConversationId(@Path("id") id: String): Call<BasicResponseBody>

    @GET("profile/{id}")
    suspend fun getProfileById(@Path("id") id: String): ProfileResponseBody

    @GET("search/get/people/{name}")
    suspend fun getProfilesByName(@Path("name") name: String): ProfileListResponseBody

    @GET("post/{id}")
    suspend fun getPost(@Path("id") id: String): PostResponseBody

    @GET("payment/transaction/{id}")
    fun getTransactionById(@Path("id") id: String): Call<TransactionResponseBody>
}