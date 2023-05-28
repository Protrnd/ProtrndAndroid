package protrnd.com.data.network.backgroundtask

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.responses.ProfileResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SaveProfileService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {
    override fun doWork(): Result {
        val authToken = inputData.getString("authToken")!!
        val profile = inputData.getString("profile")!!
        try {
            val db = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java,authToken)
            val type = object : TypeToken<ProfileDTO>() {}.type
            val currentProfile: ProfileDTO = Gson().fromJson(profile, type)
            db.updateProfileSynchronous(currentProfile).enqueue(object : Callback<ProfileResponseBody> {
                override fun onResponse(
                    call: Call<ProfileResponseBody>,
                    response: Response<ProfileResponseBody>
                ) {

                }

                override fun onFailure(call: Call<ProfileResponseBody>, t: Throwable) {

                }
            })
        } catch (_: Exception) {
            return Result.failure()
        }
        return Result.success()
    }
}