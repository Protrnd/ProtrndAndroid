package protrnd.com.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import protrnd.com.data.models.Notification
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.network.database.NotificationDatabase
import protrnd.com.data.network.database.ProfileDatabase
import protrnd.com.data.pagingsource.NotificationsPagingSource

class NotificationRepository(
    private val notificationApi: NotificationApi,
    db: NotificationDatabase,
    profileDatabase: ProfileDatabase
) : BaseRepository() {

    private val dao = db.notificationDao()

    private val profileDao = profileDatabase.profileDao()

    private val source = NotificationsPagingSource(notificationApi)

    suspend fun saveNotifications(notifications: List<Notification>) {
        dao.deleteAllNotifications()
        dao.insertNotifications(notifications)
    }

    fun getSavedProfile(id: String) = profileDao.getProfile(id)

    fun getSavedNotifications() = dao.getAllPosts()

    fun getNotificationsPage() = Pager(
        config = PagingConfig(pageSize = 20, maxSize = 200, enablePlaceholders = false),
        pagingSourceFactory = { source }
    ).liveData

    suspend fun getProfileById(id: String) = safeApiCall { notificationApi.getProfileById(id) }

    suspend fun setNotificationViewed(id: String) =
        safeApiCall { notificationApi.setNotificationViewed(id) }
}