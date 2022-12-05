package protrnd.com.ui.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.data.responses.GetNotificationsResponseBody

class NotificationViewModel(val repository: NotificationRepository): ViewModel() {
    private val _notifications: MutableLiveData<Resource<GetNotificationsResponseBody>> = MutableLiveData()
    val notifications: LiveData<Resource<GetNotificationsResponseBody>>
        get() = _notifications

    fun getNotificationsPage(page: Int) = viewModelScope.launch {
        _notifications.value = Resource.Loading
        _notifications.value = repository.getNotificationsPage(page)
    }

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun setNotificationViewed(id: String) = repository.setNotificationViewed(id)
}