package protrnd.com.data.network.backgroundtask

import android.content.Context
import androidx.work.WorkerParameters
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.responses.BasicResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeletePostService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {
    override fun doWork(): Result {
        val id = inputData.getString("id")!!
        val authToken = inputData.getString("token")!!
        val api = ProtrndAPIDataSource().buildAPI(PostApi::class.java, authToken)
        api.deletePost(id).enqueue(object : Callback<BasicResponseBody> {
            override fun onResponse(
                call: Call<BasicResponseBody>,
                response: Response<BasicResponseBody>
            ) {
            }

            override fun onFailure(call: Call<BasicResponseBody>, t: Throwable) {
            }
        })
        return Result.success()
    }
}