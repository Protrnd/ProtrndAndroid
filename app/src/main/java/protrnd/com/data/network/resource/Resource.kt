package protrnd.com.data.network.resource

import okhttp3.ResponseBody

sealed class Resource<out T>(
    var data: @UnsafeVariance T? = null,
    val error: Throwable? = null
) {
    data class Success<out T>(val value: T) : Resource<T>(value)
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()

    data class Loading<out T>(val value: T? = null) : Resource<T>(value)
    data class Error<T>(val throwable: Throwable, val value: T? = null) :
        Resource<T>(value, throwable)
}