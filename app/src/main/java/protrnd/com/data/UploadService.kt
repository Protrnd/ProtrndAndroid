package protrnd.com.data

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.Location
import protrnd.com.data.models.PostDTO
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PostRepository
import protrnd.com.ui.getFileTypes
import protrnd.com.ui.getParcelable
import protrnd.com.ui.getParcelableArrayList
import protrnd.com.ui.sendUploadNotification

class UploadService : BaseService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            try {
                val authToken = intent.getStringExtra("auth_token")
                val postUriList = intent.getParcelableArrayList<Uri>("posts")
                val currentUserProfile = intent.getParcelable<Profile>("profile")
                val caption = intent.getStringExtra("caption")
                val postApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java, authToken)
                val profileApi = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, authToken)
                val repo = PostRepository(profileApi, postApi)
                lifecycleScope.launch {
                    val result = postUriList?.let {
                        repo.addImageToFirebase(
                            it,
                            currentUserProfile!!.username,
                            this@UploadService.applicationContext.getFileTypes(postUriList)
                        )
                    }
                    val l = currentUserProfile!!.location!!.split(",")
                    val location = Location(cities = listOf(l[1]), state = l[0])
                    val postDto = PostDTO(
                        caption = caption.toString(),
                        location = location,
                        uploadurls = result!!
                    )
                    when (val add = repo.addPost(postDto)) {
                        is Resource.Success -> {
                            if (add.value.successful) {
                                sendUploadNotification(currentUserProfile, add.value.data.id)
                            } else {
                                sendUploadNotification(currentUserProfile, "")
                            }
                        }
                        is Resource.Failure -> {
                            sendUploadNotification(currentUserProfile, "")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "An error occurred please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return START_NOT_STICKY
    }
}