package protrnd.com.data.network

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Location
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.ui.getFileTypes

class ApiRequestService(context: Context, params: WorkerParameters) : androidx.work.Worker(context, params) {

    override fun doWork(): Result {
        val authToken = inputData.getString("auth")!!
        val caption = inputData.getString("caption")!!
        val tags = inputData.getStringArray("tags")!!
        val postUriList = inputData.getStringArray("uris")!!
        val username = inputData.getString("name")!!
        val city = inputData.getString("city")!!
        val state = inputData.getString("state")!!
        val location = Location(city = city, state = state)
        val api = ProtrndAPIDataSource().buildAPI(PostApi::class.java, authToken)
        val uriList = arrayListOf<Uri>()
        postUriList.forEach {
            uriList.add(Uri.parse(it))
        }
        CoroutineScope(Dispatchers.IO).launch {
            val result = uriList.let {
                uploadImage(
                    it,
                    username,
                    applicationContext.getFileTypes(it)
                )
            }

            val postDto = PostDTO(
                caption = caption,
                location = location,
                uploadurls = result,
                tags = tags.toList()
            )

            api.addPost(postDto)
        }
        return Result.success()
    }

    suspend fun uploadImage(uris: List<Uri>, username: String, fileType: List<String>): List<String> {
        return addImageToFirebase(uris, username, fileType)
    }

    suspend fun addImageToFirebase(
        uris: List<Uri>,
        username: String,
        fileType: List<String>
    ): List<String> {
        val urls = mutableListOf<String>()
        for (position in uris.indices) {
            withContext(Dispatchers.IO) {
                try {
                    val fileReference: StorageReference =
                        FirebaseStorage.getInstance().reference.child(
                            username +
                                    System.currentTimeMillis()
                                        .toString() + "." + fileType[position]
                        )
                    val downloadUrl =
                        fileReference.putFile(uris[position]).await().storage.downloadUrl.await()
                    urls.add(downloadUrl.toString())
                } catch (e: Exception) {
                    throw e
                }
            }
        }
        return urls
    }
}