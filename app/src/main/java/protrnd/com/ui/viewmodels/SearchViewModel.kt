package protrnd.com.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.SearchRepository
import protrnd.com.data.responses.GetPostsResponseBody
import protrnd.com.data.responses.ProfileListResponseBody

class SearchViewModel(private val repo: SearchRepository) : ViewModel() {
    private val _profiles: MutableLiveData<Resource<ProfileListResponseBody>> = MutableLiveData()
    val profiles: LiveData<Resource<ProfileListResponseBody>>
        get() = _profiles

    private val _posts: MutableLiveData<Resource<GetPostsResponseBody>> = MutableLiveData()
    val posts: LiveData<Resource<GetPostsResponseBody>>
        get() = _posts

    fun searchProfilesByName(name: String) = viewModelScope.launch {
        _profiles.value = Resource.Loading()
        _profiles.value = repo.searchProfilesByName(name)
    }

    fun searchPostsByName(name: String) = viewModelScope.launch {
        _posts.value = Resource.Loading()
        _posts.value = repo.searchPostsByName(name)
    }

    suspend fun getPost(id: String) = repo.getPost(id)
}