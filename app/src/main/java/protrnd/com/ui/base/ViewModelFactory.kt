package protrnd.com.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import protrnd.com.data.repository.*
import protrnd.com.ui.auth.AuthViewModel
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.notification.NotificationViewModel
import protrnd.com.ui.post.PostViewModel

class ViewModelFactory (private val repository: BaseRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository as HomeRepository) as T
            modelClass.isAssignableFrom(PostViewModel::class.java) -> PostViewModel(repository as PostRepository) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(repository as NotificationRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass not found")
        }
    }
}