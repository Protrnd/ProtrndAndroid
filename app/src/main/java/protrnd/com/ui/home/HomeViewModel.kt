package protrnd.com.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.data.responses.*

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    private val _profile: MutableLiveData<Resource<ProfileResponseBody>> = MutableLiveData()
    val profile: LiveData<Resource<ProfileResponseBody>>
        get() = _profile

    private val _locations: MutableLiveData<Resource<GetLocationResponseBody>> = MutableLiveData()
    val locations: LiveData<Resource<GetLocationResponseBody>>
        get() = _locations

    private val _isFollowing: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val isFollowing: LiveData<Resource<BasicResponseBody>>
        get() = _isFollowing

    private val _comments: MutableLiveData<Resource<GetCommentsResponseBody>> = MutableLiveData()
    val comments: LiveData<Resource<GetCommentsResponseBody>>
        get() = _comments

    fun getCurrentProfile() = viewModelScope.launch {
        _profile.value = Resource.Loading
        _profile.value = repository.getCurrentProfile()
    }

    fun updateProfile(profileDTO: ProfileDTO) = viewModelScope.launch {
        _profile.value = Resource.Loading
        _profile.value = repository.updateProfile(profileDTO)
    }

    suspend fun getPostByPage(page: Int) = repository.getPostsPage(page)

    suspend fun getPostsQueried(page: Int, word: String) = repository.getPostsQuery(page, word)

    suspend fun getQueryCount(word: String) = repository.getQueryCount(word)

    suspend fun getPost(id: String) = repository.getPost(id)

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun getProfileByName(name: String) = repository.getProfileByUsername(name)

    suspend fun getProfilePosts(id: String) = repository.getProfilePosts(id)

    suspend fun likePost(id: String) = repository.likePost(id)

    suspend fun postIsLiked(id: String): LiveData<Resource<LikeResponseBody>> {
        val postIsLikedI: MutableLiveData<Resource<LikeResponseBody>> = MutableLiveData()
        val postIsLiked: LiveData<Resource<LikeResponseBody>> = postIsLikedI
        postIsLikedI.postValue(repository.isPostLiked(id))
        return postIsLiked
    }

    suspend fun unlikePost(id: String) = repository.unlikePost(id)

    suspend fun getLikesCount(id: String) = repository.getLikesCount(id)

    fun getLocations() = viewModelScope.launch { _locations.value = repository.getLocations() }

    suspend fun uploadImage(uri: Uri, username: String, filetype: String): LiveData<String> {
        val add = MutableLiveData<String>()
        val a: LiveData<String> = add
        add.postValue(repository.addImageToFirebase(uri, username, filetype))
        return a
    }

    suspend fun getFollowersCount(id: String) = repository.getFollowersCount(id)

    suspend fun getFollowingsCount(id: String) = repository.getFollowingsCount(id)

    fun isFollowing(id: String) = viewModelScope.launch {
        _isFollowing.value = Resource.Loading
        _isFollowing.value = repository.isFollowing(id)
    }

    suspend fun follow(id: String) = repository.follow(id)

    suspend fun unfollow(id: String) = repository.unfollow(id)

    suspend fun addComment(commentDTO: CommentDTO) = repository.addComment(commentDTO)

    fun getComments(id: String) = viewModelScope.launch {
        _comments.value = Resource.Loading
        _comments.value = repository.getComments(id)
    }
}