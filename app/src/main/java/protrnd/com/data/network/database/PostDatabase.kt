package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import protrnd.com.data.models.ListTypeConverter
import protrnd.com.data.models.Post
import protrnd.com.data.network.dao.PostDao

@Database(entities = [Post::class], version = 1)
@TypeConverters(ListTypeConverter::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}