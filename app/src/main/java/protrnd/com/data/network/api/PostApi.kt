package protrnd.com.data.network.api

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import protrnd.com.data.models.*
import protrnd.com.data.responses.*
import retrofit2.http.*

interface PostApi {
    @GET("post/fetch/{page}")
    suspend fun getPosts(@Path("page") page: Int): GetPostsResponseBody

    @GET("search/get/posts/{name}")
    suspend fun getPostsByName(@Path("name") name: String): GetPostsResponseBody

    @GET("post/fetch/promotions/{page}")
    suspend fun getPromotionsPage(@Path("page") page: Int): PromotionListResponseBody

    @GET("post/fetch/query")
    suspend fun getPostsQueried(
        @Query("page") page: Int,
        @Query("word") word: String
    ): GetPostsResponseBody

    @GET("post/profile/tags")
    suspend fun getProfilePostTags(
        @Query("page", encoded = true) page: Int,
        @Query("profileid", encoded = true) profileid: String
    ): GetPostsResponseBody

    @GET("post/get/count/{word}")
    suspend fun getQueryCount(@Path(value = "word", encoded = true) word: String): BasicResponseBody

    @GET("post/is-liked/{id}")
    suspend fun postIsLiked(@Path("id") id: String): BooleanResponseBody

    @POST("post/like/{id}")
    suspend fun likePost(@Path("id") id: String): BooleanResponseBody

    @DELETE("post/delete/like/{id}")
    suspend fun unlikePost(@Path("id") id: String): BooleanResponseBody

    @GET("post/{id}/like-count")
    suspend fun getLikesCount(@Path("id") id: String): BasicResponseBody

    @POST("post/add")
    suspend fun addPost(@Body postDTO: PostDTO): PostResponseBody

    @POST("post/comment")
    suspend fun addComment(@Body commentDTO: CommentDTO): CommentResponseBody

    @GET("post/{id}/comments")
    suspend fun getComments(@Path("id") id: String): GetCommentsResponseBody

    @GET("post/{id}/posts")
    suspend fun getProfilePosts(@Path("id") id: String): GetPostsResponseBody

    @GET("post/{id}")
    suspend fun getPost(@Path("id") id: String): PostResponseBody
}