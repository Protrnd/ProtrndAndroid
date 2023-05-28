package protrnd.com.data.network.api

import protrnd.com.data.models.*
import protrnd.com.data.responses.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {
    @POST("chat/send")
    fun sendChat(@Body chat: ChatDTO): Call<BooleanResponseBody>

    @POST("payment/verify/promotion")
    suspend fun verifyPromotion(@Body verifyPromotion: VerifyPromotion): BasicResponseBody

    @POST("payment/support/transfer")
    suspend fun supportPost(@Body supportDTO: SupportDTO): BasicResponseBody

    @POST("payment/support/virtual")
    suspend fun virtualMoneySupportPost(@Body supportDTO: SupportDTO): BasicResponseBody

    @GET("payment/balance/{id}")
    suspend fun getBalance(@Path("id") id: String): IntDataResponseBody

    @GET("payment/transactions/{page}")
    suspend fun getTransactionsPaginated(@Path("page") page: Int): TransactionsListResponseBody

    @POST("payment/funds/withdraw")
    suspend fun withdrawFunds(@Body withdraw: WithdrawDTO): BasicResponseBody

    @POST("payment/topup")
    suspend fun topUpFunds(@Body fundsDTO: FundsDTO): BasicResponseBody

    @POST("payment/balance/to")
    suspend fun sendProtrndFunds(@Body fundsDTO: FundsDTO): BasicResponseBody

    @POST("payment/set/{pin}")
    suspend fun setProtrndPin(@Path("pin") pin: String): BasicResponseBody

    @POST("payment/forgot/pin")
    suspend fun sendResetOTPForPin(): BasicResponseBody

    @GET("payment/correct/{pin}")
    suspend fun isProtrndPinCorrect(@Path("pin") pin: String): BooleanResponseBody

    @GET("payment/get/pin")
    suspend fun isPinAvailable(): BooleanResponseBody

    @GET("search/get/people/{name}")
    suspend fun getProfilesByName(@Path("name") name: String): ProfileListResponseBody

    @GET("profile/{id}")
    suspend fun getProfileById(@Path("id") id: String): ProfileResponseBody
}