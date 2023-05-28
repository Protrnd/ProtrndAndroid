package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import protrnd.com.data.models.Conversation
import protrnd.com.data.network.dao.ConversationDao

@Database(entities = [Conversation::class], version = 1)
abstract class ConversationDatabase: RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}