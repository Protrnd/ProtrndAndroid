package protrnd.com.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.ActivityNotificationBinding
import protrnd.com.ui.adapter.NotificationAdapter
import protrnd.com.ui.adapter.NotificationTabAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity
import protrnd.com.ui.isNetworkAvailable

class NotificationActivity :
    BaseActivity<ActivityNotificationBinding, NotificationViewModel, NotificationRepository>() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationLayoutManager: LinearLayoutManager

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.appToolbar)
        val actionBar = supportActionBar!!
        actionBar.title = "Notifications"
        actionBar.setDisplayHomeAsUpEnabled(true)
        binding.appToolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)

        val titles = arrayOf("All", "Socials", "Transactions")
        binding.notificationsPager.adapter = NotificationTabAdapter(supportFragmentManager,lifecycle)
        TabLayoutMediator(binding.notificationsTabs,binding.notificationsPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

//        notificationLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        notificationAdapter =
//            NotificationAdapter(viewModel = viewModel, activity = this, lifecycleOwner = this)
//        setupRecyclerView()
//        loadMoreItems()
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

//    private fun loadMoreItems() {
//        if (isNetworkAvailable()) {
//            viewModel.getNotificationsPage().observe(this) {
//                notificationAdapter.submitData(lifecycle, it)
//            }
//        } else {
//            viewModel.getSavedNotifications().asLiveData().observe(this) {
//                if (it.isNotEmpty()) {
//                    notificationAdapter.submitData(lifecycle, PagingData.from(it))
//                }
//            }
//        }
//    }

//    private fun setupRecyclerView() {
//        binding.notificationRecycler.apply {
//            this.adapter = notificationAdapter
//            this.layoutManager = notificationLayoutManager
//        }
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
//        lifecycleScope.launch {
//            val items = notificationAdapter.snapshot().items
//            if (items.isNotEmpty())
//                viewModel.saveNotifications(items)
//        }
    }
}