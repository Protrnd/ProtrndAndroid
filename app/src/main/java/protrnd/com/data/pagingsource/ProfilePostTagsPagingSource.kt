package protrnd.com.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import protrnd.com.data.models.Post
import protrnd.com.data.network.api.PostApi
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class ProfilePostTagsPagingSource(private val api: PostApi, private val profileId: String) :
    PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = api.getProfilePostTags(page = position, profileid = profileId)
            val posts = response.data
            LoadResult.Page(
                data = posts,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (posts.isEmpty()) null else position + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}