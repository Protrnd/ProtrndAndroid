package protrnd.com.ui.post

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.PostRepository
import protrnd.com.data.responses.PostResponseBody
import protrnd.com.data.responses.ProfileListResponseBody
import protrnd.com.data.responses.ProfileResponseBody

class PostViewModel(private val repository: PostRepository
) : ViewModel() {
    private val _profile: MutableLiveData<Resource<ProfileResponseBody>> = MutableLiveData()
    val profile: LiveData<Resource<ProfileResponseBody>>
        get() = _profile

    private val _uploadUrl: MutableLiveData<List<String>> = MutableLiveData()
    val uploadUrl: LiveData<List<String>>
        get() = _uploadUrl

    private val _profiles: MutableLiveData<Resource<ProfileListResponseBody>> = MutableLiveData()
    val profiles: LiveData<Resource<ProfileListResponseBody>>
        get() = _profiles

    private val _postResult: MutableLiveData<Resource<PostResponseBody>> = MutableLiveData()
    val postResult: LiveData<Resource<PostResponseBody>>
        get() = _postResult

    fun uploadImage(uri: List<Uri>, username: String, filetype: List<String>) = viewModelScope.launch {
        _uploadUrl.value = repository.addImageToFirebase(uri, username, filetype)
    }

    fun getProfilesByUsername(name: String) = viewModelScope.launch {
        _profiles.value = repository.getProfilesByUsername(name)
    }

    fun getCurrentProfile() = viewModelScope.launch {
        _profile.value = Resource.Loading
        _profile.value = repository.getCurrentProfile()
    }

    fun addPost(postDTO: PostDTO) = viewModelScope.launch {
        _postResult.value = Resource.Loading
        _postResult.value = repository.addPost(postDTO)
    }
}