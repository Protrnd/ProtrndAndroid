package protrnd.com.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.RegisterDTO
import protrnd.com.data.models.VerifyOTP
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.data.responses.BasicResponseBody

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _loginResponse: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val loginResponse: LiveData<Resource<BasicResponseBody>>
        get() = _loginResponse

    private val _registerResponse: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val registerResponse: LiveData<Resource<BasicResponseBody>>
        get() = _registerResponse

    private val _verifyOtpResponse: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val verifyOtpResponse: LiveData<Resource<BasicResponseBody>>
        get() = _verifyOtpResponse

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading()
        _loginResponse.value = repository.login(email, password)
    }

    suspend fun saveAuthToken(token: String) = repository.saveAuthToken(token)

    suspend fun register(registerDTO: RegisterDTO) = viewModelScope.launch {
        _registerResponse.value = Resource.Loading()
        _registerResponse.value = repository.register(registerDTO)
    }

    suspend fun verifyOtp(verifyOTP: VerifyOTP) = viewModelScope.launch {
        _verifyOtpResponse.value = Resource.Loading()
        _verifyOtpResponse.value = repository.verifyOtp(verifyOTP)
    }
}