package protrnd.com.data.network.api

import protrnd.com.data.responses.PromotionListResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface PromotionApi {
    @GET("post/fetch/promotions/{page}")
    suspend fun getPromotionsPage(@Path("page") page: Int): PromotionListResponseBody
}