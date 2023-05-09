package protrnd.com.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.ChatRepository
import protrnd.com.data.responses.ProfileListResponseBody
import protrnd.com.data.responses.ProfileResponseBody

class ChatViewModel(val repository: ChatRepository) : ViewModel() {
    private val receiverProfile: MutableLiveData<Resource<ProfileResponseBody>> = MutableLiveData()
    val _receiverProfile: MutableLiveData<Resource<ProfileResponseBody>>
        get() = receiverProfile

    private val searchProfile: MutableLiveData<Resource<ProfileListResponseBody>> =
        MutableLiveData()
    val _searchProfile: MutableLiveData<Resource<ProfileListResponseBody>>
        get() = searchProfile

    fun getConversations() = repository.getConversations()

    fun getChatConversationData(id: String) = repository.getChatConversationData(id)

    fun sendChat(chatDTO: ChatDTO) = repository.sendChat(chatDTO)

    fun getProfile(id: String) = viewModelScope.launch {
        receiverProfile.value = Resource.Loading()
        receiverProfile.value = repository.getProfileById(id)
    }

    fun searchProfilesByName(name: String) = viewModelScope.launch {
        searchProfile.value = Resource.Loading()
        searchProfile.value = repository.searchProfilesByName(name)
    }

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun getPost(id: String) = repository.getPost(id)

    fun getTransactionById(id: String) = repository.getTransactionById(id)
}