package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.ConversationId

@Dao
interface ConversationIdDao {
    @Query("SELECT * FROM ConversationId WHERE profileid = :profileid")
    fun getConversationId(profileid: String): Flow<ConversationId>

    @Query("SELECT * FROM ConversationId")
    fun getConversationIds(): Flow<List<ConversationId>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationId(conversationId: ConversationId)

    @Query("DELETE FROM ConversationId")
    suspend fun deleteAllConversationId()
}