package protrnd.com.ui.auth

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.databinding.ActivityAuthenticationBinding
import protrnd.com.ui.finishActivity
import protrnd.com.ui.handleUnCaughtException

class AuthenticationActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthenticationBinding
    lateinit var navController: NavController
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            binding.root.handleUnCaughtException()
        }
        setSupportActionBar(binding.appAuthToolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setIcon(R.drawable.launcher_outlined_ic)
        actionBar.title = " Protrnd"
        navHost = supportFragmentManager.findFragmentById(R.id.authContainerView) as NavHostFragment
        navController = navHost.navController
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val isLoginFragment = isLoginFragment()
                if (isLoginFragment)
                    finishActivity()
                else
                    navController.popBackStack()
            }
        })
    }

    fun isLoginFragment(): Boolean {
        return try {
            navHost.childFragmentManager.fragments.any { it.javaClass == LoginFragment::class.java && it.isVisible }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun startFragment(navDirections: NavDirections) {
        navController.navigate(navDirections)
    }
}