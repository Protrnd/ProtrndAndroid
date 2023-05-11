package protrnd.com.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.databinding.ActivityWelcomeBinding
import protrnd.com.ui.adapter.OnBoardingPagerAdapter
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.handleUnCaughtException
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.startActivityFromNotification
import protrnd.com.ui.startNewActivityWithNoBackstack

class WelcomeActivity : AppCompatActivity() {
    lateinit var profilePreferences: ProfilePreferences
    lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        supportRequestWindowFeature(FEATURE_NO_TITLE)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        setContentView(binding.root)
        profilePreferences = ProfilePreferences(this)
        val profile = runBlocking { profilePreferences.profile.first() }
        val authToken = runBlocking { profilePreferences.authToken.first() }
        var currentUserProfile = Profile()
        if (authToken != null && profile != null) {
            currentUserProfile = Gson().fromJson(profile, Profile::class.java)
        }
        val bundle = intent!!.extras
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            binding.root.handleUnCaughtException(e)
        }
        if (bundle != null && bundle.containsKey("post_id")) {
            bundle.putBoolean("isFromNotification", true)
            startActivityFromNotification(PostActivity::class.java, bundle)
        } else if (currentUserProfile.id.isNotEmpty()) {
            startNewActivityWithNoBackstack(HomeActivity::class.java)
        }

        binding.onboardingFragmentsPager.adapter = OnBoardingPagerAdapter()
        binding.getStartedBtn.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }
    }
}