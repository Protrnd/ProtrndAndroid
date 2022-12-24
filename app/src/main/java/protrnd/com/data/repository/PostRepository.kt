package protrnd.com.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi

class PostRepository(private val api: ProfileApi, private val postsApi: PostApi) :
    BaseRepository() {

    suspend fun addPost(postDTO: PostDTO) = safeApiCall { postsApi.addPost(postDTO) }

    suspend fun getProfilesByUsername(name: String) =
        safeApiCall { api.getProfilesByUsername(name) }

    suspend fun addImageToFirebase(
        uris: List<Uri>,
        username: String,
        fileType: List<String>
    ): List<String> {
        val urls = mutableListOf<String>()
        for (position in uris.indices) {
            try {
                val fileReference: StorageReference = FirebaseStorage.getInstance().reference.child(
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
        return urls
    }
}