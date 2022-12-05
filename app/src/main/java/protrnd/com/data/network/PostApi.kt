package protrnd.com.data.network

import protrnd.com.data.models.*
import protrnd.com.data.responses.*
import retrofit2.http.*

interface PostApi {
    @GET("post/fetch/{page}")
    suspend fun getPosts(@Path("page") page: Int): GetPostsResponseBody

    @GET("post/is-liked/{id}")
    suspend fun postIsLiked(@Path("id") id: String): LikeResponseBody

    @POST("post/like/{id}")
    suspend fun likePost(@Path("id") id: String): LikeResponseBody

    @DELETE("post/delete/like/{id}")
    suspend fun unlikePost(@Path("id") id: String): LikeResponseBody

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