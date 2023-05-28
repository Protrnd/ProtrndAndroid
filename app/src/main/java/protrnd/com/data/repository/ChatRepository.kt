package protrnd.com.data.repository

import protrnd.com.data.models.*
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.network.database.ChatDatabase
import protrnd.com.data.network.database.ConversationDatabase
import protrnd.com.data.network.database.ConversationIdDatabase
import protrnd.com.data.network.database.ProfileDatabase

class ChatRepository(private val api: ChatApi, chatDb: ChatDatabase? = null, conversationDb: ConversationDatabase? = null, conversationIdDb: ConversationIdDatabase? = null, profileDb: ProfileDatabase? = null) : BaseRepository() {
    private val chatDao = chatDb?.chatDao()
    private val convoDao = conversationDb?.conversationDao()
    private val conversationIdDb = conversationIdDb?.conversationIdDao()
    private val profileDb = profileDb?.profileDao()

    fun getStoredProfile(id: String) = profileDb?.getProfile(id)

    fun getConversationStoredId(profileId: String) = conversationIdDb?.getConversationId(profileId)

    suspend fun saveProfile(profile: Profile) = profileDb?.insertProfile(profile)

    suspend fun storeConversationId(conversationId: ConversationId) = conversationIdDb?.insertConversationId(conversationId)

    fun sendChat(chatDTO: ChatDTO) = api.sendChat(chatDTO)

    fun getConversations() = api.getConversations()

    fun getChatConversationData(id: String) = api.getChatData(id)

    fun getConversationId(id: String) = api.getConversationId(id)

    suspend fun getProfileById(id: String) = safeApiCall { api.getProfileById(id) }

    suspend fun searchProfilesByName(name: String) = safeApiCall { api.getProfilesByName(name) }

    suspend fun getPost(id: String) = safeApiCall { api.getPost(id) }

    fun getTransactionById(id: String) = api.getTransactionById(id)

    suspend fun insetChat(chat: List<Chat>) = chatDao?.insertChat(chat)

    fun getChat(convoid: String) = chatDao?.getChat(convoid)

    fun getConversationsStored() = convoDao?.getConversations()

    suspend fun insertConversations(conversations: List<Conversation>) = convoDao?.insertConversations(conversations)
}