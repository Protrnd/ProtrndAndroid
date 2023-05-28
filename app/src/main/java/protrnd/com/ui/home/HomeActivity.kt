package protrnd.com.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHomeBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.chat.ChatFragment
import protrnd.com.ui.notification.NotificationActivity
import protrnd.com.ui.profile.ProfileFragment
import protrnd.com.ui.search.SearchFragment
import protrnd.com.ui.startAnimation
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.wallet.WalletFragment

class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel, HomeRepository>() {

    private var previousTag = "H"
    private var bottomNavPreviousId: Int = R.id.home
    private var previousFragment: Fragment? = null
    private lateinit var bottomNav: RadioGroup
    private var homeFragment: HomeFragment? = null
    private var profileFragment: ProfileFragment? = null
    private var walletFragment: WalletFragment? = null
    private var chatFragment: ChatFragment? = null
    private var searchFragment: SearchFragment? = null

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
        registerMessaging()
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setIcon(R.drawable.launcher_outlined_ic)
        actionBar.title = " Protrnd"

        bottomNav = binding.bottomNav
        setBottomNavSelectedItem()
        if (homeFragment == null)
            homeFragment = HomeFragment()
        homeFragment?.showFragment("H")
    }

    fun setChatChecked() {
        binding.chatSelector.isChecked = true
    }

    private fun setBottomNavSelectedItem() {
        bottomNav.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.profile_selector -> {
                    if (profileFragment == null)
                        profileFragment = ProfileFragment()
                    profileFragment?.showFragment("P")
                }
                R.id.search_selector -> {
                    if (searchFragment == null)
                        searchFragment = SearchFragment()
                    searchFragment?.showFragment("S")
                }
                R.id.home_selector -> {
                    if (homeFragment == null)
                        homeFragment = HomeFragment()
                    homeFragment?.showFragment("H")
                }
                R.id.wallet_selector -> {
                    if (walletFragment == null)
                        walletFragment = WalletFragment()
                    walletFragment?.showFragment("W")
                }
                R.id.chat_selector -> {
                    if (chatFragment == null)
                        chatFragment = ChatFragment()
                    chatFragment?.showFragment("C")
                }
            }
            bottomNavPreviousId = checkedId
        }
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
                startAnimation()
            }
        }
        return true
    }

    private fun registerMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic(currentUserProfile.identifier)
    }

    override fun onResume() {
        super.onResume()
        if (bottomNav.checkedRadioButtonId != R.id.home_selector) {
            setBottomNavSelectedItem()
            bottomNav.check(bottomNavPreviousId)
        }
    }

    private fun Fragment.showFragment(tag: String) {
        val fm = supportFragmentManager
        val fragment = fm.findFragmentByTag(tag)
        previousTag = tag
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
            if (fragment != null && fragment is ChatFragment)
                ft.show(this)
            else
                ft.add(R.id.fragmentContainerView, this, tag)
        } else {
            if (fragment != null && fragment is SearchFragment)
                ft.show(this)
            else
                ft.add(R.id.fragmentContainerView, this, tag)
        }
        previousFragment = this
        ft.commit()
    }
}