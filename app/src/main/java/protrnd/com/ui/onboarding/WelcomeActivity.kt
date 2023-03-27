package protrnd.com.ui.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import protrnd.com.databinding.ActivityWelcomeBinding
import protrnd.com.ui.adapter.OnBoardingPagerAdapter
import protrnd.com.ui.auth.AuthenticationActivity

class WelcomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(2000)
        installSplashScreen()
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        supportRequestWindowFeature(FEATURE_NO_TITLE)
        window.setFlags(FLAG_FULLSCREEN,FLAG_FULLSCREEN)
        setContentView(binding.root)

        binding.onboardingFragmentsPager.adapter = OnBoardingPagerAdapter()
        binding.getStartedBtn.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }
    }
}