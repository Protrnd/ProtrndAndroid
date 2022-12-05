package protrnd.com

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import protrnd.com.data.ProfilePreferences
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.startNewActivityFromAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val profilePreferences = ProfilePreferences(this)
        profilePreferences.authToken.asLiveData().observe(this) {
            val activity = if (it == null) AuthenticationActivity::class.java else HomeActivity::class.java
            startNewActivityFromAuth(activity)
        }
    }
}