package protrnd.com.data.repository

import protrnd.com.data.network.api.NotificationApi

class NotificationRepository(private val notificationApi: NotificationApi) : BaseRepository() {

    suspend fun getNotificationsPage(page: Int) =
        safeApiCall { notificationApi.getNotificationPaginated(page) }

    suspend fun getProfileById(id: String) = safeApiCall { notificationApi.getProfileById(id) }

    suspend fun setNotificationViewed(id: String) =
        safeApiCall { notificationApi.setNotificationViewed(id) }
}