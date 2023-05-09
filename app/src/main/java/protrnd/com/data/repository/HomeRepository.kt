package protrnd.com.data.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.database.PostDatabase
import protrnd.com.data.network.database.ProfileDatabase
import protrnd.com.data.pagingsource.HashTagsPagingSource
import protrnd.com.data.pagingsource.PostsPagingSource
import protrnd.com.data.pagingsource.ProfilePostTagsPagingSource

class HomeRepository(
    private val api: ProfileApi,
    private val postsApi: PostApi,
    val db: PostDatabase? = null,
    profileDatabase: ProfileDatabase? = null
) : BaseRepository() {

    private val postDao = db?.postDao()

    private val profileDao = profileDatabase?.profileDao()

    fun getPostsPage() = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100, enablePlaceholders = false),
        pagingSourceFactory = { PostsPagingSource(postsApi) }
    ).liveData

//    fun getPostsPageNetworkResource() = networkBoundResource(
//        query = { postDao.getAllPosts() },
//        fetch = {
//            delay(2000)
//            getPostsPage()
//        },
//        saveFetchResult = {
//            db?.withTransaction {
//                postDao.deleteAllPosts()
////                postDao.insertPosts()
//            }
//        }
//    )

    fun getHashTagPage(word: String) = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100, enablePlaceholders = false),
        pagingSourceFactory = { HashTagsPagingSource(postsApi, word) }
    ).liveData

    fun getProfilePostTagsPage(profileId: String) = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100, enablePlaceholders = false),
        pagingSourceFactory = { ProfilePostTagsPagingSource(postsApi, profileId) }
    ).liveData

    fun retry() = PostsPagingSource(postsApi).invalidate()

    suspend fun searchProfilesByName(name: String) = safeApiCall { api.getProfilesByName(name) }

    suspend fun searchPostsByName(name: String) = safeApiCall { postsApi.getPostsByName(name) }

    suspend fun savePostResult(posts: List<Post>) {
        postDao?.deleteAllPosts()
        postDao?.insertPosts(posts)
    }

    suspend fun saveProfile(profile: Profile) {
        val dbSize = profileDao?.getDBSize()?.first()
        if (dbSize != null && dbSize >= 20) {
            val profiles = profileDao?.getProfiles()!!
            profileDao.deleteProfile(profiles.first()[(0..20).random()].id)
        }
        profileDao?.insertProfile(profile)
    }

    fun getSavedPosts() = postDao?.getAllPosts()

    fun getProfile(id: String) = profileDao?.getProfile(id)

    suspend fun getCurrentProfile() = safeApiCall { api.getCurrentProfile() }

    suspend fun getProfileById(id: String) = safeApiCall { api.getProfileById(id) }

    suspend fun getPromotionsPage(page: Int) = safeApiCall { postsApi.getPromotionsPage(page) }

    suspend fun getProfileByUsername(name: String) = safeApiCall { api.getProfileByName(name) }

    suspend fun getQueryCount(word: String) = safeApiCall { postsApi.getQueryCount(word) }

    suspend fun isPostLiked(postId: String) = safeApiCall { postsApi.postIsLiked(postId) }

    suspend fun likePost(postId: String) = safeApiCall { postsApi.likePost(postId) }

    suspend fun unlikePost(postId: String) = safeApiCall { postsApi.unlikePost(postId) }

    suspend fun getLikesCount(postId: String) = safeApiCall { postsApi.getLikesCount(postId) }

    suspend fun getLocations() = safeApiCall { api.getLocations() }

    suspend fun updateProfile(profile: ProfileDTO) = safeApiCall { api.updateProfile(profile) }

    suspend fun addImageToFirebase(uris: Uri, username: String, fileType: String): String {
        try {
            val fileReference: StorageReference = FirebaseStorage.getInstance().reference.child(
                username +
                        System.currentTimeMillis()
                            .toString() + "." + fileType
            )
            val downloadUrl = fileReference.putFile(uris).await().storage.downloadUrl.await()
            return downloadUrl.toString()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getFollowersCount(id: String) = safeApiCall { api.getFollowersCount(id) }

    suspend fun getFollowingsCount(id: String) = safeApiCall { api.getFollowingCount(id) }

    suspend fun isFollowing(id: String) = safeApiCall { api.isFollowing(id) }

    suspend fun follow(id: String) = safeApiCall { api.follow(id) }

    suspend fun unfollow(id: String) = safeApiCall { api.unfollow(id) }

    suspend fun addComment(commentDTO: CommentDTO) = safeApiCall { postsApi.addComment(commentDTO) }

    suspend fun getComments(id: String) = safeApiCall { postsApi.getComments(id) }

    suspend fun getProfilePosts(id: String) = safeApiCall { postsApi.getProfilePosts(id) }

    suspend fun getPost(id: String) = safeApiCall { postsApi.getPost(id) }
}