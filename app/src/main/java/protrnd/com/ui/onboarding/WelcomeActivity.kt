package protrnd.com.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Constants
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.databinding.ActivityWelcomeBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.OnBoardingPagerAdapter
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.post.PostActivity
import java.util.*

class WelcomeActivity : AppCompatActivity() {
    lateinit var profilePreferences: ProfilePreferences
    lateinit var binding: ActivityWelcomeBinding

    companion object {
        const val DELAY = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        supportRequestWindowFeature(FEATURE_NO_TITLE)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        setContentView(binding.root)
        val bundle = intent!!.extras
        registerNewsletter()
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            runOnUiThread {
                binding.progressbar.visible(false)
                binding.mainView.visible(true)
                binding.root.handleUnCaughtException()
            }
        }
        profilePreferences = ProfilePreferences(this)
        CoroutineScope(Dispatchers.IO).launch {
            val profile = profilePreferences.profile.first()
            val authToken = profilePreferences.authToken.first()
            var currentUserProfile = Profile()
            try {
                if (authToken != null && profile != null) {
                    currentUserProfile = Gson().fromJson(profile, Profile::class.java)
                }
                if (authToken != null) {
                    val api = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, authToken)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isNetworkAvailable()) {
                            val timer = Timer()
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    runOnUiThread {
                                        runAction(bundle, currentUserProfile)
                                        timer.cancel()
                                    }
                                }
                            }, DELAY)
                            val result = api.getProfileById(currentUserProfile.id)
                            if (!result.successful) {
                                logout()
                                withContext(Dispatchers.Main) {
                                    timer.cancel()
                                    binding.progressbar.visible(false)
                                    binding.mainView.visible(true)
                                    binding.root.errorSnackBar("You have been logged out because your account has been disabled or does not exist with protrnd")
                                }
                                startNewActivityWithNoBackstack(AuthenticationActivity::class.java)
                            } else {
                                withContext(Dispatchers.Main) {
                                    timer.cancel()
                                    runAction(bundle, currentUserProfile)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                runAction(bundle, currentUserProfile)
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        runAction(bundle, currentUserProfile)
                    }
                }
            } catch (_: Exception) {
                binding.progressbar.visible(false)
                binding.mainView.visible(true)
            }
        }
        binding.onboardingFragmentsPager.adapter = OnBoardingPagerAdapter()
        binding.getStartedBtn.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }
    }

    fun runAction(bundle: Bundle?, currentUserProfile: Profile) {
        if (bundle != null && bundle.containsKey("post_id")) {
            bundle.putBoolean("isFromNotification", true)
            startActivityFromNotification(PostActivity::class.java, bundle)
        } else if (currentUserProfile.id.isNotEmpty()) {
            startNewActivityWithNoBackstack(HomeActivity::class.java)
        } else {
            binding.progressbar.visible(false)
            binding.mainView.visible(true)
        }
    }
    private fun registerNewsletter() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.NEWSLETTER)
    }
}