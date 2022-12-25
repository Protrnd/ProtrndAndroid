package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import protrnd.com.data.models.Notification
import protrnd.com.data.network.dao.NotificationDao

@Database(entities = [Notification::class], version = 1)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}