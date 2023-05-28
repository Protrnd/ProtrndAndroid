package protrnd.com.data.network.backgroundtask

import android.app.Application
import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import protrnd.com.data.models.Chat
import protrnd.com.data.network.ProtrndAPIDataSource

class SaveMessagesService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {

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
        val messages = inputData.getString("chatMessages")!!
        val db = ProtrndAPIDataSource().provideChatDatabase(getApplication())
        val type = object : TypeToken<List<Chat>>() {}.type
        db.chatDao().insertChat(Gson().fromJson(messages, type))
        return Result.success()
    }
}