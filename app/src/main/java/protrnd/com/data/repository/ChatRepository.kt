package protrnd.com.data.repository

import protrnd.com.data.models.ChatDTO
import protrnd.com.data.network.api.ChatApi

class ChatRepository(private val api: ChatApi) : BaseRepository() {
    fun sendChat(chatDTO: ChatDTO) = api.sendChat(chatDTO)

    fun getConversations() = api.getConversations()

    fun getChatConversationData(id: String) = api.getChatData(id)

    suspend fun getProfileById(id: String) = safeApiCall { api.getProfileById(id) }

    suspend fun searchProfilesByName(name: String) = safeApiCall { api.getProfilesByName(name) }

    suspend fun getPost(id: String) = safeApiCall { api.getPost(id) }

    fun getTransactionById(id: String) = api.getTransactionById(id)
}