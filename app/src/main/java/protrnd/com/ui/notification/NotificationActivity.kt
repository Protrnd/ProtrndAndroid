package protrnd.com.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.ActivityNotificationBinding
import protrnd.com.ui.adapter.NotificationTabAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity
import protrnd.com.ui.viewmodels.NotificationViewModel

class NotificationActivity :
    BaseActivity<ActivityNotificationBinding, NotificationViewModel, NotificationRepository>() {

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.appToolbar)
        val actionBar = supportActionBar!!
        actionBar.title = "Notifications"
        actionBar.setDisplayHomeAsUpEnabled(true)
        binding.appToolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)

        val titles = arrayOf("All", "Transactions")
        binding.notificationsPager.adapter =
            NotificationTabAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.notificationsTabs, binding.notificationsPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityNotificationBinding.inflate(inflater)

    override fun getViewModel() = NotificationViewModel::class.java

    override fun getActivityRepository(): NotificationRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = ProtrndAPIDataSource().buildAPI(NotificationApi::class.java, token)
        val db = ProtrndAPIDataSource().provideNotificationDatabase(application)
        val profileDb = ProtrndAPIDataSource().provideProfileDatabase(application)
        return NotificationRepository(api, db, profileDb)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}