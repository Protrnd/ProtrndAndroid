package protrnd.com.ui.viewmodels

import androidx.lifecycle.ViewModel
import protrnd.com.data.models.Notification
import protrnd.com.data.repository.NotificationRepository

class NotificationViewModel(val repository: NotificationRepository) : ViewModel() {

    fun getNotificationsPage() = repository.getNotificationsPage()

    fun getProfile(id: String) = repository.getSavedProfile(id)

    suspend fun saveNotifications(notifications: List<Notification>) {
        repository.saveNotifications(notifications)
    }

    fun getSavedNotifications() = repository.getSavedNotifications()

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun setNotificationViewed(id: String) = repository.setNotificationViewed(id)

}