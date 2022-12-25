package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.Notification

@Dao
interface NotificationDao {
    @Query("SELECT * FROM Notifications")
    fun getAllPosts(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(posts: List<Notification>)

    @Query("DELETE FROM Notifications")
    suspend fun deleteAllNotifications()
}