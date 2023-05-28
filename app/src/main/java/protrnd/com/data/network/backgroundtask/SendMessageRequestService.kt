package protrnd.com.data.network.backgroundtask

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import protrnd.com.R
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.responses.BooleanResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendMessageRequestService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {
    override fun doWork(): Result {
        val authToken = inputData.getString("auth")!!
        val chatMessage = inputData.getString("chatMessage")!!
        val profileid = inputData.getString("profileid")!!
        val convoid = inputData.getString("convoid")!!
        val type = inputData.getString("type")!!
        val api = ProtrndAPIDataSource().buildAPI(ChatApi::class.java, authToken)
        CoroutineScope(Dispatchers.IO).launch {
            val dto = ChatDTO(
                itemid = profileid,
                message = chatMessage,
                receiverid = profileid,
                convoid = convoid,
                type = type
            )
            api.sendChat(dto).enqueue(object : Callback<BooleanResponseBody> {
                override fun onResponse(
                    call: Call<BooleanResponseBody>,
                    response: Response<BooleanResponseBody>
                ) {

                }

                override fun onFailure(call: Call<BooleanResponseBody>, t: Throwable) {
                }
            })
        }
        return Result.success()
    }
}