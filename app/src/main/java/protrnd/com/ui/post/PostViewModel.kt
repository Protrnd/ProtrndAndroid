package protrnd.com.ui.post

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PostRepository
import protrnd.com.data.responses.ProfileListResponseBody
import protrnd.com.data.responses.ProfileResponseBody

class PostViewModel(
    private val repository: PostRepository
) : ViewModel() {
    private val _profile: MutableLiveData<Resource<ProfileResponseBody>> = MutableLiveData()
    val profile: LiveData<Resource<ProfileResponseBody>>
        get() = _profile

    private val _profiles: MutableLiveData<Resource<ProfileListResponseBody>> = MutableLiveData()
    val profiles: LiveData<Resource<ProfileListResponseBody>>
        get() = _profiles

    suspend fun uploadImage(uri: List<Uri>, username: String, filetype: List<String>) =
        repository.addImageToFirebase(uri, username, filetype)

    fun getProfilesByUsername(name: String) = viewModelScope.launch {
        _profiles.value = Resource.Loading()
        _profiles.value = repository.getProfilesByUsername(name)
    }

    suspend fun addPost(postDTO: PostDTO) = repository.addPost(postDTO)
}