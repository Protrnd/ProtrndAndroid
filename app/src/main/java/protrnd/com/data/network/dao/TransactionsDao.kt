package protrnd.com.data.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import protrnd.com.data.models.Transaction

@Dao
interface TransactionsDao {
    @Query("SELECT COUNT(*) FROM Transactions")
    fun getTransactionsDBSize(): Flow<Int>

    @Insert(onConflict = REPLACE)
    fun insertTransactions(transactions: List<Transaction>)

    @Query("SELECT * FROM Transactions")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM Transactions WHERE id = :id")
    fun getTransaction(id: String): Flow<Transaction?>

    @Query("DELETE FROM Transactions")
    suspend fun deleteAllTransactions()
}