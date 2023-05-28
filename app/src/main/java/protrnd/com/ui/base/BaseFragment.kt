package protrnd.com.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.repository.BaseRepository
import protrnd.com.ui.handleUnCaughtException
import java.util.regex.Pattern

abstract class BaseFragment<VM : ViewModel, B : ViewBinding, R : BaseRepository> : Fragment() {
    protected lateinit var binding: B
    protected val protrndAPIDataSource = ProtrndAPIDataSource()
    protected lateinit var viewModel: VM
    lateinit var profilePreferences: ProfilePreferences
    var currentUserProfile: Profile = Profile()
    var token = ""
    var paymentPin = ""
    var followers = "0"
    var followings = "0"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profilePreferences = ProfilePreferences(requireContext())
        binding = getFragmentBinding(inflater, container)
        CoroutineScope(Dispatchers.IO).launch {
            val authToken =
                profilePreferences.authToken.first() //This automatically stores the information to memory
            val profile = profilePreferences.profile.first()
            val pin = profilePreferences.pin.first()
            val followersP = profilePreferences.followers.first()
            val followingsP = profilePreferences.followings.first()
            if (pin != null)
                paymentPin = pin
            if (authToken != null && profile != null) {
                token = authToken
                currentUserProfile = Gson().fromJson(profile, Profile::class.java)
            }
            if (followersP != null)
                followers = followersP
            if (followingsP != null)
                followings = followingsP
        }
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        onViewReady(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            binding.root.handleUnCaughtException()
        }
        return binding.root
    }

    @CallSuper
    protected open fun onViewReady(savedInstanceState: Bundle?) {
        //To be used by child fragments
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract fun getFragmentRepository(): R

    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }

    fun isValidEmail(str: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }
}