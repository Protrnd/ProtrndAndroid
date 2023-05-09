package protrnd.com.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import protrnd.com.R
import protrnd.com.databinding.ActivityAuthenticationBinding
import protrnd.com.ui.finishActivity
import protrnd.com.ui.handleUnCaughtException

class AuthenticationActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthenticationBinding
    lateinit var navController: NavController
    private lateinit var navHost: NavHostFragment
    private lateinit var dexter: DexterBuilder
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            dexter.check()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestNetworkPermissions()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dexter = Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.INTERNET
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        report.let {
                            if (!report.areAllPermissionsGranted()) {
                                val requestNotificationIntent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        val uri = Uri.fromParts("package", packageName, null)
                                        data = uri
                                    }
                                resultLauncher.launch(requestNotificationIntent)
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                })
            dexter.check()
        }

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            binding.root.handleUnCaughtException(e)
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


    private fun requestNetworkPermissions() {
        val requestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestLauncher.launch(Manifest.permission.INTERNET)
            }
        }
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