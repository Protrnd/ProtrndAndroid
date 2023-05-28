package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Post

@Dao
interface ChatDao {
    @Query("SELECT * FROM Chat WHERE convoid = :convoid")
    fun getChat(convoid: String): Flow<List<Chat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chats: List<Chat>)

    @Query("DELETE FROM Chat")
    suspend fun deleteAllChat()
}