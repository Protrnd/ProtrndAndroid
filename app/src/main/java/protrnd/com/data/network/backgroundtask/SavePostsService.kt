package protrnd.com.data.network.backgroundtask

import android.app.Application
import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import protrnd.com.data.models.Post
import protrnd.com.data.network.ProtrndAPIDataSource

class SavePostsService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {

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
        val messages = inputData.getString("posts")!!
        try {
            val db = ProtrndAPIDataSource().providePostDatabase(getApplication())
            val type = object : TypeToken<List<Post>>() {}.type
            db.postDao().insertPosts(Gson().fromJson(messages, type))
        } catch (_: Exception) {}
        return Result.success()
    }
}