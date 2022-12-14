package protrnd.com.ui.home

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import protrnd.com.R
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHomeBinding
import protrnd.com.databinding.SelectPaymentActionBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.checkStoragePermissions
import protrnd.com.ui.notification.NotificationActivity
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.profile.ProfileFragmentDirections

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel, HomeRepository>() {

    private lateinit var navController: NavController

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setIcon(R.drawable.launcher_outlined_ic)
        actionBar.title = " Protrnd"
        val chipNavigationBar = binding.bottomNav
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController
        chipNavigationBar.setItemSelected(R.id.home)
        chipNavigationBar.setOnItemSelectedListener { itemId ->
            if (itemId == R.id.profile)
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
            else
                navController.navigate(ProfileFragmentDirections.actionProfileFragmentToHomeFragment())
        }

        this.checkStoragePermissions()

        binding.fab.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val isHomeFragment =
                    navHost.childFragmentManager.fragments.any { it.javaClass == HomeFragment::class.java && it.isVisible }
                if (isHomeFragment)
                    finish()
                else {
                    chipNavigationBar.setItemSelected(R.id.home)
                }
            }
        })
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityHomeBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository(): HomeRepository {
        val profileApi = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java)
        val postsApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java)
        return HomeRepository(profileApi, postsApi)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_toolbar_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification_btn -> {
                startActivity(Intent(this, NotificationActivity::class.java))
            }
            R.id.scan_btn -> {
                val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
                val qrBinding = SelectPaymentActionBinding.inflate(layoutInflater)
                dialog.setContentView(qrBinding.root)
                dialog.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            }
        }
        return true
    }
}