package protrnd.com.data.network

import protrnd.com.data.models.Login
import protrnd.com.data.models.RegisterDTO
import protrnd.com.data.models.VerifyOTP
import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.data.responses.PayloadResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @GET("auth")
    suspend fun getCurrentProfilePayload(): PayloadResponseBody

    @POST("auth/login/{type}")
    suspend fun login(
        @Path("type") type: String, @Body login: Login
    ): BasicResponseBody

    @POST("auth/register")
    suspend fun register(@Body registerDTO: RegisterDTO): BasicResponseBody

    @POST("auth/verify/otp")
    suspend fun verifyOtp(@Body verifyOTP: VerifyOTP): BasicResponseBody
}