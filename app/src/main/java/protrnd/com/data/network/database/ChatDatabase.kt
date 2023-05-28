package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Post
import protrnd.com.data.network.dao.ChatDao

@Database(entities = [Chat::class], version = 1)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao
}