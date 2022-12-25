package protrnd.com.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import protrnd.com.data.models.Notification
import protrnd.com.data.network.api.NotificationApi
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class NotificationsPagingSource(private val api: NotificationApi) :
    PagingSource<Int, Notification>() {
    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notification> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = api.getNotificationPaginated(position)
            val notifications = response.data
            LoadResult.Page(
                data = notifications,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (notifications.isEmpty()) null else position + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}