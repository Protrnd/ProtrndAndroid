package protrnd.com.data.network.api

import protrnd.com.data.models.Login
import protrnd.com.data.models.RegisterDTO
import protrnd.com.data.models.VerifyOTP
import protrnd.com.data.responses.BasicResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login/{type}")
    suspend fun login(
        @Path("type") type: String, @Body login: Login
    ): BasicResponseBody

    @POST("auth/register")
    suspend fun register(@Body registerDTO: RegisterDTO): BasicResponseBody

    @POST("auth/verify/otp")
    suspend fun verifyOtp(@Body verifyOTP: VerifyOTP): BasicResponseBody
}