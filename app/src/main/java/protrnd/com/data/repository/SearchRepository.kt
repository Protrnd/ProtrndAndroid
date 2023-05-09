package protrnd.com.data.repository

import protrnd.com.data.network.api.SearchApi

class SearchRepository(val api: SearchApi) : BaseRepository() {
    suspend fun getPost(id: String) = safeApiCall { api.getPost(id) }

    suspend fun searchProfilesByName(name: String) = safeApiCall { api.getProfilesByName(name) }

    suspend fun searchPostsByName(name: String) = safeApiCall { api.getPostsByName(name) }
}