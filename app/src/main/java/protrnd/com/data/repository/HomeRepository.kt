package protrnd.com.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi

class HomeRepository(private val api: ProfileApi, private val postsApi: PostApi) :
    BaseRepository() {

    suspend fun getCurrentProfile() = safeApiCall { api.getCurrentProfile() }

    suspend fun getProfileById(id: String) =
        safeApiCall { api.getProfileById(id) }

    suspend fun getPostsPage(page: Int) = safeApiCall { postsApi.getPosts(page) }

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