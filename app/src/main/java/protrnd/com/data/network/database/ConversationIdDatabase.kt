package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import protrnd.com.data.models.ConversationId
import protrnd.com.data.network.dao.ConversationIdDao

@Database(entities = [ConversationId::class], version = 1)
abstract class ConversationIdDatabase: RoomDatabase() {
    abstract fun conversationIdDao(): ConversationIdDao
}