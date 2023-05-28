package protrnd.com.data.network.backgroundtask

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.ProtrndAPIDataSource

class SaveTransactionsService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {
    companion object {
        var app: Application? = null
        private fun getApplication(): Application {
            return app!!
        }
        fun setApplication(application: Application) {
            app = application
        }
    }

    override fun doWork(): Result {
        val messages = inputData.getString("transactions")!!
        val db = ProtrndAPIDataSource().provideTransactionDatabase(getApplication())
        val type = object : TypeToken<List<Transaction>>() {}.type
        db.transactionDao().insertTransactions(Gson().fromJson(messages, type))
        return Result.success()
    }
}