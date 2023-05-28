package protrnd.com.data.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import protrnd.com.data.models.*
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.database.ProfileDatabase
import protrnd.com.data.network.database.TransactionsDatabase
import protrnd.com.data.pagingsource.TransactionsPagingSource
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val api: PaymentApi,
    db: TransactionsDatabase? = null,
    profiledb: ProfileDatabase? = null
) : BaseRepository() {

    private val transactionDao = db?.transactionDao()

    private val profileDao = profiledb?.profileDao()

    fun getAllTransactions() = transactionDao?.getAllTransactions()

    fun getProfile(id: String) = profileDao?.getProfile(id)

    fun getSavedProfileByName(name: String) = profileDao?.getSavedProfileByName(name)

    suspend fun saveProfile(profile: Profile) = profileDao?.insertProfile(profile)

    fun getTransaction(id: String) = transactionDao?.getTransaction(id)

    suspend fun setPaymentPin(pin: String) = safeApiCall { api.setProtrndPin(pin) }

    suspend fun setResetPinOTP() = safeApiCall { api.sendResetOTPForPin() }

    suspend fun isPinCorrect(pin: String) = safeApiCall { api.isProtrndPinCorrect(pin) }

    suspend fun isPinAvailable() = safeApiCall { api.isPinAvailable() }

    fun sendChat(chatDTO: ChatDTO) = api.sendChat(chatDTO)

    suspend fun verifyPromotionPayment(verifyPromotion: VerifyPromotion) =
        safeApiCall { api.verifyPromotion(verifyPromotion) }

    suspend fun searchProfilesByName(name: String) = safeApiCall { api.getProfilesByName(name) }

    suspend fun getProfileById(id: String) = safeApiCall { api.getProfileById(id) }

    suspend fun supportPost(supportDTO: SupportDTO) = safeApiCall { api.supportPost(supportDTO) }

    suspend fun virtualSupportPost(supportDTO: SupportDTO) =
        safeApiCall { api.virtualMoneySupportPost(supportDTO) }

    suspend fun getBalance(id: String) = safeApiCall { api.getBalance(id) }

    suspend fun withdrawFunds(withdraw: WithdrawDTO) = safeApiCall { api.withdrawFunds(withdraw) }

    suspend fun topUpFunds(fundsDTO: FundsDTO) = safeApiCall { api.topUpFunds(fundsDTO) }

    suspend fun sendProtrndFunds(fundsDTO: FundsDTO) =
        safeApiCall { api.sendProtrndFunds(fundsDTO) }

    fun getTransactionsPage() = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { TransactionsPagingSource(api) }
    ).liveData

    suspend fun getTransactionPage(page: Int) = safeApiCall { api.getTransactionsPaginated(page) }

//    suspend fun saveTransactions(transactions: List<Transaction>) {
//        val dbSize = transactionDao.getTransactionsDBSize().first()
//        if (transactions.size > dbSize) {
//            transactionDao.insertTransactions(transactions.toMutableList().slice(dbSize..transactions.size))
//        }
//    }

    suspend fun addImageToFirebase(
        uris: Uri,
        username: String
    ): String {
        val url: String
        try {
            val fileReference: StorageReference = FirebaseStorage.getInstance().reference.child(
                username + "promotion" +
                        System.currentTimeMillis()
                            .toString() + ".jpg"
            )
            val downloadUrl =
                fileReference.putFile(uris).await().storage.downloadUrl.await()
            url = downloadUrl.toString()
        } catch (e: Exception) {
            throw e
        }
        return url
    }

//    fun getSavedTransactions() = transactionDao.getAllTransactions()

//    fun getPostsPageNetworkResource(transactions: List<Transaction>) = networkBoundResource(
//        query = { transactionDao.getAllTransactions() },
//        fetch = {
//            delay(2000)
//            getTransactionsPage()
//        },
//        saveFetchResult = {
//            db.withTransaction {
//                transactionDao.insertTransactions(transactions)
//            }
//        }
//    )
}