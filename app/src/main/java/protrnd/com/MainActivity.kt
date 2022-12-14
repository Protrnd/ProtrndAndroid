package protrnd.com

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import protrnd.com.data.ProfilePreferences
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityMainBinding
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.handleAPIError
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.startNewActivityFromAuth

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var lvm: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            val profilePreferences = ProfilePreferences(this)
            profilePreferences.authToken.asLiveData().observe(this) {
                if (it != null) {
                    val api = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, it)
                    val postsApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java, it)
                    lvm = HomeViewModel(HomeRepository(api, postsApi))
                    getCurrentProfile()
                    lvm.profile.observe(this) { profileResponse ->
                        when (profileResponse) {
                            is Resource.Success -> {
                                lifecycleScope.launch {
                                    profilePreferences.saveProfile(profileResponse.value.data)
                                    startNewActivityFromAuth(HomeActivity::class.java)
                                }
                            }
                            is Resource.Failure -> {
                                handleAPIError(
                                    binding.root,
                                    profileResponse
                                ) { getCurrentProfile() }
                            }
                            else -> {}
                        }
                    }
                } else {
                    startNewActivityFromAuth(AuthenticationActivity::class.java)
                }
            }
        } catch (e: Exception) {
            startNewActivityFromAuth(AuthenticationActivity::class.java)
        }
    }

    private fun getCurrentProfile() {
        lvm.getCurrentProfile()
    }
}