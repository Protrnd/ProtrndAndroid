package protrnd.com.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import protrnd.com.data.repository.*
import protrnd.com.ui.viewmodels.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: BaseRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository as HomeRepository) as T
            modelClass.isAssignableFrom(PostViewModel::class.java) -> PostViewModel(repository as PostRepository) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(
                repository as NotificationRepository
            ) as T
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> PaymentViewModel(repository as PaymentRepository) as T
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(repository as ChatRepository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(repository as SearchRepository) as T
            else -> throw IllegalArgumentException("ViewModelClass not found")
        }
    }
}