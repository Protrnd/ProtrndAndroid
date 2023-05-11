package protrnd.com.data.repository

import protrnd.com.data.models.Login
import protrnd.com.data.models.RegisterDTO
import protrnd.com.data.models.ResetPasswordDTO
import protrnd.com.data.models.VerifyOTP
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.data.network.api.AuthApi

class AuthRepository(private val api: AuthApi, private val preferences: ProfilePreferences) :
    BaseRepository() {
    suspend fun login(email: String, password: String) = safeApiCall {
        api.login("jwt", Login(email, password))
    }

    suspend fun saveAuthToken(token: String) {
        preferences.saveAuthToken(token)
    }

    suspend fun register(registerDTO: RegisterDTO) = safeApiCall {
        api.register(registerDTO)
    }

    suspend fun verifyOtp(verifyOTP: VerifyOTP) = safeApiCall { api.verifyOtp(verifyOTP) }

    suspend fun forgotPassword(login: Login) = safeApiCall { api.forgotPassword(login) }

    suspend fun resetPassword(reset: ResetPasswordDTO) = safeApiCall { api.resetPassword(reset) }
}