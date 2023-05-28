package protrnd.com.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi

class PostRepository(private val api: ProfileApi, private val postsApi: PostApi) :
    BaseRepository() {

}