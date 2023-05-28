package protrnd.com.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.*
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.data.responses.BooleanResponseBody
import protrnd.com.data.responses.IntDataResponseBody
import protrnd.com.data.responses.ProfileListResponseBody
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(val repository: PaymentRepository) : ViewModel() {

    private val balance: MutableLiveData<Resource<IntDataResponseBody>> = MutableLiveData()
    val _balance: LiveData<Resource<IntDataResponseBody>>
        get() = balance

    private val withdrawF: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val _withdraw: LiveData<Resource<BasicResponseBody>>
        get() = withdrawF

    private val _profiles: MutableLiveData<Resource<ProfileListResponseBody>> = MutableLiveData()
    val profiles: LiveData<Resource<ProfileListResponseBody>>
        get() = _profiles

    private val transactionsLiveData: MutableLiveData<MutableList<Transaction>> = MutableLiveData()
    val transactions: LiveData<MutableList<Transaction>> = transactionsLiveData

    private val isPinAvailable: MutableLiveData<Resource<BooleanResponseBody>> = MutableLiveData()
    val pinAvailable: LiveData<Resource<BooleanResponseBody>>
        get() = isPinAvailable

    fun searchProfilesByName(name: String) = viewModelScope.launch {
        _profiles.value = Resource.Loading()
        _profiles.value = repository.searchProfilesByName(name)
    }

    fun getAllTransactions() = repository.getAllTransactions()

    suspend fun saveProfile(profile: Profile) = repository.saveProfile(profile)

    fun getSavedProfileByName(name: String) = repository.getSavedProfileByName(name)

    fun getSavedProfile(id: String) = repository.getProfile(id)

    suspend fun getProfileByName(name: String) = repository.searchProfilesByName(name)

    suspend fun getProfileById(id: String) =
        withContext(Dispatchers.IO) { repository.getProfileById(id) }

    suspend fun verifyPromotionPayment(verifyPromotion: VerifyPromotion) =
        withContext(Dispatchers.IO) { repository.verifyPromotionPayment(verifyPromotion) }

    suspend fun supportPost(supportDTO: SupportDTO) =
        withContext(Dispatchers.IO) { repository.supportPost(supportDTO) }

    suspend fun virtualMoneySupportPost(supportDTO: SupportDTO) =
        repository.virtualSupportPost(supportDTO)

    fun getBalance(id: String) = viewModelScope.launch {
        balance.value = repository.getBalance(id)
    }

    fun sendPaymentChat(chatDTO: ChatDTO) = repository.sendChat(chatDTO)

    suspend fun setPaymentPin(pin: String) = repository.setPaymentPin(pin)

    suspend fun setResetOTPForPin() = repository.setResetPinOTP()

    suspend fun isPaymentPinCorrect(pin: String) = repository.isPinCorrect(pin)

    suspend fun isPinAvailable() = viewModelScope.launch {
        isPinAvailable.value = repository.isPinAvailable()
    }

    fun getTransactionsPage() = repository.getTransactionsPage().cachedIn(viewModelScope)

//    fun getTransactionsNetworkResource(transactions: List<Transaction>) = repository.getPostsPageNetworkResource(
//        transactions
//    )

    suspend fun getTransactionsPage(page: Int) = viewModelScope.launch {
        when (val response = repository.getTransactionPage(page)) {
            is Resource.Success -> {
                if (response.data != null && response.data!!.successful) {
                    transactionsLiveData.postValue(response.data!!.data.toMutableList())
                    MemoryCache.transactionsMap = response.data!!.data.toMutableList()
                }
            }
            is Resource.Loading -> {
                val transactions = MemoryCache.transactionsMap
                transactionsLiveData.postValue(transactions)
            }
            else -> {}
        }
    }

    suspend fun uploadImage(uri: Uri, username: String) = withContext(Dispatchers.IO) {
        repository.addImageToFirebase(uri, username)
    }

    fun withdrawFunds(withdraw: WithdrawDTO) = viewModelScope.launch {
        withdrawF.value = Resource.Loading()
        withdrawF.value = repository.withdrawFunds(withdraw)
    }

    suspend fun topUpFunds(fundsDTO: FundsDTO) =
        withContext(Dispatchers.IO) { repository.topUpFunds(fundsDTO) }

    suspend fun sendProtrndFunds(fundsDTO: FundsDTO) =
        withContext(Dispatchers.IO) { repository.sendProtrndFunds(fundsDTO) }
}