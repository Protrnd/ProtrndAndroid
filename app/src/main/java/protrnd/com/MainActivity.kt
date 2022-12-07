package protrnd.com

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import protrnd.com.data.ProfilePreferences
import protrnd.com.databinding.ActivityMainBinding
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.startNewActivityFromAuth

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val profilePreferences = ProfilePreferences(this)
            profilePreferences.authToken.asLiveData().observe(this) {
                val activity =
                    if (it == null) AuthenticationActivity::class.java else HomeActivity::class.java
                startNewActivityFromAuth(activity)
            }
        } catch (e: Exception) {
            startNewActivityFromAuth(AuthenticationActivity::class.java)
        }
    }
}