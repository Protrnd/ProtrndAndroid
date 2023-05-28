package protrnd.com.data.network.api

import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.data.responses.GetLocationResponseBody
import protrnd.com.data.responses.ProfileListResponseBody
import protrnd.com.data.responses.ProfileResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ProfileApi {
    @GET("profile")
    suspend fun getCurrentProfile(): ProfileResponseBody

    @GET("search/get/people/{name}")
    suspend fun getProfilesByName(@Path("name") name: String): ProfileListResponseBody

    @GET("profile/{id}")
    suspend fun getProfileById(@Path("id") id: String): ProfileResponseBody

    @GET("profile/fetch/{name}")
    suspend fun getProfileByName(@Path("name") name: String): ProfileResponseBody

    @GET("profile/name/{name}")
    suspend fun getProfilesByUsername(@Path("name") name: String): ProfileListResponseBody

    @GET("location/get")
    suspend fun getLocations(): GetLocationResponseBody

    @PUT("profile/update")
    suspend fun updateProfile(@Body profileDTO: ProfileDTO): ProfileResponseBody

    @PUT("profile/update")
    fun updateProfileSynchronous(@Body profileDTO: ProfileDTO): Call<ProfileResponseBody>

    @GET("profile/followers/{id}/count")
    suspend fun getFollowersCount(@Path("id") id: String): BasicResponseBody

    @GET("profile/followings/{id}/count")
    suspend fun getFollowingCount(@Path("id") id: String): BasicResponseBody

    @GET("profile/is-following/{id}")
    suspend fun isFollowing(@Path("id") id: String): BasicResponseBody

    @POST("profile/follow/{id}")
    suspend fun follow(@Path("id") id: String): BasicResponseBody

    @DELETE("profile/unfollow/{id}")
    suspend fun unfollow(@Path("id") id: String): BasicResponseBody
}