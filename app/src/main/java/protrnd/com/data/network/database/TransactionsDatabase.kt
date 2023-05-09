package protrnd.com.data.network.database

import androidx.room.Database
import androidx.room.RoomDatabase
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.dao.TransactionsDao

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class TransactionsDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionsDao
}