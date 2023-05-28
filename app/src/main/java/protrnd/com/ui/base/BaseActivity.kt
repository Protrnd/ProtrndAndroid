package protrnd.com.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.BaseRepository
import protrnd.com.ui.*
import protrnd.com.ui.auth.AuthenticationActivity
import java.util.*

abstract class BaseActivity<B : ViewBinding, VM : ViewModel, R : BaseRepository> :
    AppCompatActivity() {
    lateinit var viewModel: VM
    lateinit var profilePreferences: ProfilePreferences
    protected val protrndAPIDataSource = ProtrndAPIDataSource()
    lateinit var binding: B
    val profileHash = HashMap<String, Profile>()
    var currentUserProfile: Profile = Profile()
    open var authToken: String? = null
    var isAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getActivityBinding(layoutInflater)
        setContentView(binding.root)
        this.checkStoragePermissions()
        profilePreferences = ProfilePreferences(this)
        var profileP: String? = null
        val timer = Timer()
        lifecycleScope.launch {
            profilePreferences.saveLoginTime(Date().time)
        }
        timer.schedule(object : TimerTask(){
            override fun run() {
                if (authToken != null) {
                    val api = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, authToken)
                    NetworkConnectionLiveData(this@BaseActivity).observe(this@BaseActivity) { networkAvailable ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (networkAvailable) {
                                val result = api.getProfileById(currentUserProfile.id)
                                if (!result.successful) {
                                    binding.root.errorSnackBar("You have been logged out because your account has been disabled or does not exist with protrnd")
                                    startNewActivityWithNoBackstack(AuthenticationActivity::class.java)
                                    Toast.makeText(this@BaseActivity, "Please login", Toast.LENGTH_SHORT).show()
                                } else {
                                    profilePreferences.saveLoginTime(Date().time)
                                    currentUserProfile = result.data
                                }
                            }
                        }
                    }
                }
            }
        }, HOUR_IN_MILLIS)

        CoroutineScope(Dispatchers.IO).launch {
            profileP = profilePreferences.profile.first()
            authToken = profilePreferences.authToken.first()
            if (authToken != null && profileP != null) {
                currentUserProfile = Gson().fromJson(profileP, Profile::class.java)
            }
        }
        val factory = ViewModelFactory(getActivityRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        onViewReady(savedInstanceState, intent)
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            binding.root.handleUnCaughtException()
        }
        if (authToken == null && profileP == null) {
            if (!isAuth) {
                startNewActivityWithNoBackstack(AuthenticationActivity::class.java)
                Toast.makeText(this@BaseActivity, "Please login", Toast.LENGTH_SHORT).show()
            }
        } else if (authToken == null || authToken!!.isEmpty()) {
            if (!isAuth) {
                startNewActivityWithNoBackstack(AuthenticationActivity::class.java)
                Toast.makeText(this@BaseActivity, "Please login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    abstract fun getActivityBinding(inflater: LayoutInflater): B

    abstract fun getViewModel(): Class<VM>

    abstract fun getActivityRepository(): R

    @CallSuper
    protected open fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        //To be used by child activities
    }
}