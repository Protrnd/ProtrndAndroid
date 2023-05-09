package protrnd.com.data.network.api

import protrnd.com.data.responses.GetPostsResponseBody
import protrnd.com.data.responses.PostResponseBody
import protrnd.com.data.responses.ProfileListResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface SearchApi {
    @GET("post/{id}")
    suspend fun getPost(@Path("id") id: String): PostResponseBody

    @GET("search/get/people/{name}")
    suspend fun getProfilesByName(@Path("name") name: String): ProfileListResponseBody

    @GET("search/get/posts/{name}")
    suspend fun getPostsByName(@Path("name") name: String): GetPostsResponseBody
}