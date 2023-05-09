package protrnd.com.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.data.responses.*

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    private val _profile: MutableLiveData<Resource<ProfileResponseBody>> = MutableLiveData()
    val profile: LiveData<Resource<ProfileResponseBody>>
        get() = _profile

    private val _isFollowing: MutableLiveData<Resource<BasicResponseBody>> = MutableLiveData()
    val isFollowing: LiveData<Resource<BasicResponseBody>>
        get() = _isFollowing

    private val _comments: MutableLiveData<Resource<GetCommentsResponseBody>> = MutableLiveData()
    val comments: LiveData<Resource<GetCommentsResponseBody>>
        get() = _comments

    private val _thumbnails: MutableLiveData<Resource<GetPostsResponseBody>> = MutableLiveData()
    val thumbnails: LiveData<Resource<GetPostsResponseBody>>
        get() = _thumbnails

    private val _profiles: MutableLiveData<Resource<ProfileListResponseBody>> = MutableLiveData()
    val profiles: LiveData<Resource<ProfileListResponseBody>>
        get() = _profiles

    private val _posts: MutableLiveData<Resource<GetPostsResponseBody>> = MutableLiveData()
    val posts: LiveData<Resource<GetPostsResponseBody>>
        get() = _posts

    fun searchProfilesByName(name: String) = viewModelScope.launch {
        _profiles.value = Resource.Loading()
        _profiles.value = repository.searchProfilesByName(name)
    }

    fun searchPostsByName(name: String) = viewModelScope.launch {
        _posts.value = Resource.Loading()
        _posts.value = repository.searchPostsByName(name)
    }

    suspend fun getCurrentProfile() = withContext(Dispatchers.IO) {
        repository.getCurrentProfile()
    }

    fun updateProfile(profileDTO: ProfileDTO) = viewModelScope.launch {
        _profile.value = Resource.Loading()
        _profile.value = repository.updateProfile(profileDTO)
    }

    fun getProfile(id: String) = repository.getProfile(id)

    fun getPostByPage() = repository.getPostsPage().cachedIn(viewModelScope)

    suspend fun getPromotionsPage(page: Int) = withContext(Dispatchers.IO) {
        repository.getPromotionsPage(page)
    }

    suspend fun savePosts(posts: List<Post>) = repository.savePostResult(posts)

    fun getSavedPosts() = repository.getSavedPosts()

    fun getPostsQueried(word: String) = repository.getHashTagPage(word).cachedIn(viewModelScope)

    fun getProfilePostTagsPage(profileId: String) =
        repository.getProfilePostTagsPage(profileId).cachedIn(viewModelScope)

    suspend fun getQueryCount(word: String) = repository.getQueryCount(word)

    suspend fun getPost(id: String) = repository.getPost(id)

    suspend fun getProfileById(id: String) = repository.getProfileById(id)

    suspend fun getProfileByName(name: String) = repository.getProfileByUsername(name)

    fun getProfilePosts(id: String) = viewModelScope.launch {
        _thumbnails.value = repository.getProfilePosts(id)
    }

    suspend fun likePost(id: String) = withContext(Dispatchers.IO) {
        repository.likePost(id)
    }

    suspend fun postIsLiked(id: String) = withContext(Dispatchers.IO) {
        repository.isPostLiked(id)
    }

    suspend fun unlikePost(id: String) = withContext(Dispatchers.IO) {
        repository.unlikePost(id)
    }

    suspend fun getLikesCount(id: String) = repository.getLikesCount(id)

    suspend fun uploadImage(uri: Uri, username: String, filetype: String): LiveData<String> {
        val add = MutableLiveData<String>()
        val a: LiveData<String> = add
        add.postValue(repository.addImageToFirebase(uri, username, filetype))
        return a
    }

    suspend fun getFollowersCount(id: String) = withContext(Dispatchers.IO) {
        repository.getFollowersCount(id)
    }

    suspend fun getFollowingsCount(id: String) = repository.getFollowingsCount(id)

    fun isFollowing(id: String) = viewModelScope.launch {
        _isFollowing.value = Resource.Loading()
        _isFollowing.value = repository.isFollowing(id)
    }

    suspend fun follow(id: String) = repository.follow(id)

    suspend fun unfollow(id: String) = repository.unfollow(id)

    suspend fun addComment(commentDTO: CommentDTO) = withContext(Dispatchers.IO) {
        repository.addComment(commentDTO)
    }

    fun getComments(id: String) = viewModelScope.launch {
        _comments.value = Resource.Loading()
        _comments.value = repository.getComments(id)
    }

    suspend fun saveProfile(data: Profile) {
        repository.saveProfile(data)
    }
}