package protrnd.com.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHomeBinding
import protrnd.com.databinding.SelectPaymentActionBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.chat.ChatFragment
import protrnd.com.ui.finishActivity
import protrnd.com.ui.notification.NotificationActivity
import protrnd.com.ui.profile.ProfileFragment
import protrnd.com.ui.showFeatureComingSoonDialog
import protrnd.com.ui.startAnimation
import protrnd.com.ui.visible
import protrnd.com.ui.wallet.WalletFragment

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel, HomeRepository>() {

    var lmState: Parcelable? = null
    private lateinit var dexter: DexterBuilder
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            dexter.check()
        }
    var previousFragment: Fragment? = null

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
//        registerMessaging()
//        requestNotificationsPermissions()
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setIcon(R.drawable.launcher_outlined_ic)
        actionBar.title = " Protrnd"
        val bottomNav = binding.bottomNav
        val homeFragment = HomeFragment()
        val profileFragment = ProfileFragment()
        val walletFragment = WalletFragment()
        val chatFragment = ChatFragment()
        homeFragment.showFragment("H")
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    profileFragment.showFragment("P")
                }
                R.id.home -> {
                    homeFragment.showFragment("H")
                }
                R.id.wallet -> {
                    walletFragment.showFragment("W")
                }
                R.id.communication -> {
                    chatFragment.showFragment("C")
                }
            }
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dexter = Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.POST_NOTIFICATIONS
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

//        bottomNav.setItemSelected(R.id.home)
//        bottomNav.setOnItemSelectedListener {
//            showHomeFragment(homeFragment, profileFragment)
//        }

//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (homeFragment.isHidden)
//                    bottomNav.setItemSelected(R.id.home)
//                else
//                    finishActivity()
//            }
//        })
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

    private fun requestNotificationsPermissions() {
        val requestLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification_btn -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                startAnimation()
            }
//            R.id.scan_btn -> {
//                binding.dimBg.visible(true)
//                binding.bottomNav.visible(false)
//                val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
//                val qrBinding = SelectPaymentActionBinding.inflate(layoutInflater)
//                dialog.setContentView(qrBinding.root)
//                qrBinding.receiveMoneyBtn.setOnClickListener {
//                    dialog.dismiss()
//                    this.showFeatureComingSoonDialog()
//                }
//                qrBinding.sendMoneyBtn.setOnClickListener {
//                    dialog.dismiss()
//                    this.showFeatureComingSoonDialog()
//                }
//                dialog.setCanceledOnTouchOutside(true)
//                dialog.setOnCancelListener {
//                    dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                    binding.dimBg.visible(false)
//                    binding.bottomNav.visible(true)
//                }
//                dialog.setOnDismissListener {
//                    dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                    binding.dimBg.visible(false)
//                    binding.bottomNav.visible(true)
//                }
//                dialog.show()
//                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                dialog.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
//            }
        }
        return true
    }

    private fun registerMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic(currentUserProfile.identifier)
    }

    private fun Fragment.showFragment(tag: String) {
        val fm = supportFragmentManager
        val fragment = fm.findFragmentByTag(tag)
        val ft = fm.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        if (previousFragment != null)
            ft.hide(previousFragment!!)
        if (this is HomeFragment) {
            if (fragment != null && fragment is HomeFragment) {
                ft.show(this)
            } else {
                ft.add(R.id.fragmentContainerView, this, tag)
            }
        } else if (this is WalletFragment) {
            if (fragment != null && fragment is WalletFragment) {
                ft.show(this)
            } else {
                ft.add(R.id.fragmentContainerView, this, tag)
            }
        } else if (this is ProfileFragment) {
            if (fragment != null && fragment is ProfileFragment) {
                ft.show(this)
            } else {
                ft.add(R.id.fragmentContainerView, this, tag)
            }
        } else if (this is ChatFragment) {
            if (fragment != null && fragment is ChatFragment) {
                ft.show(this)
            } else {
                ft.add(R.id.fragmentContainerView, this, tag)
            }
        }
        previousFragment = this
        ft.commit()
    }
//    private fun showHomeFragment(homeFragment: HomeFragment, profileFragment: ProfileFragment, walletFragment: WalletFragment) {
//        val fragmentM = supportFragmentManager.beginTransaction()
//            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//        if (homeFragment.isHidden) {
//            fragmentM.show(homeFragment)
//            fragmentM.hide(profileFragment)
//            fragmentM.hide(walletFragment)
//        } else {
//            fragmentM.hide(walletFragment)
//            fragmentM.hide(homeFragment)
//            fragmentM.show(profileFragment)
//        }
//        fragmentM.commit()
//    }
}