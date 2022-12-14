package protrnd.com.ui.notification

import androidx.lifecycle.ViewModel
import protrnd.com.data.repository.NotificationRepository

class NotificationViewModel(val repository: NotificationRepository) : ViewModel() {

    suspend fun getNotificationsPage(page: Int) = repository.getNotificationsPage(page)

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun setNotificationViewed(id: String) = repository.setNotificationViewed(id)
}