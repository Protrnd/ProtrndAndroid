package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Conversation

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversations")
    fun getConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<Conversation>)

    @Query("DELETE FROM Conversations")
    suspend fun deleteAllConversations()
}