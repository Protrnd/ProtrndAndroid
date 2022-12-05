package protrnd.com.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Notification
import protrnd.com.data.network.NotificationApi
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.ActivityNotificationBinding
import protrnd.com.ui.adapter.NotificationAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.snackbar
import protrnd.com.ui.visible

class NotificationActivity : BaseActivity<ActivityNotificationBinding,NotificationViewModel,NotificationRepository>() {

    private var page = 1
    private var isLoading = false
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

        notificationLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, true)
        loadMoreItems(true)

        binding.nestedScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            // number of visible items
            val visibleItemCount = notificationLayoutManager.childCount
            // number of items in layout
            val totalItemCount = notificationLayoutManager.itemCount
            // the position of first visible item
            val firstVisibleItemPosition = notificationLayoutManager.findFirstVisibleItemPosition()
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            // validate non negative values
            val isValidFirstItem = firstVisibleItemPosition >= 0
            // validate total items are more than possible visible items
            val totalIsMoreThanVisible = totalItemCount >= 20
            // flag to know whether to load more
            val shouldLoadMore = isValidFirstItem && isAtLastItem && totalIsMoreThanVisible
            if (shouldLoadMore) loadMoreItems(false)
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityNotificationBinding.inflate(inflater)

    override fun getViewModel() = NotificationViewModel::class.java

    override fun getActivityRepository(): NotificationRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = ProtrndAPIDataSource().buildAPI(NotificationApi::class.java,token)
        return NotificationRepository(api)
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        if (!isFirstPage)
            page += 1
        viewModel.getNotificationsPage(page)
        viewModel.notifications.observe(this) {
            lifecycleScope.launch {
                when(it) {
                    is Resource.Success -> {
                        val result = it.value.data
                        notificationAdapter = NotificationAdapter(viewModel=viewModel)
                        if (result.isEmpty())
                            return@launch
                        else if (!isFirstPage) notificationAdapter.addAll(result)
                        else notificationAdapter.setList(result as MutableList<Notification>)
                        setupRecyclerView()
                        isLoading = false
                    }
                    is Resource.Loading -> {
                        isLoading = true
                        binding.progressBar.visible(true)
                    }
                    is Resource.Failure -> {
                        isLoading = true
                        binding.progressBar.visible(true)
                        binding.root.snackbar("Error loading notifications", action = {loadMoreItems(true)})
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.notificationRecycler.apply {
            this.adapter = notificationAdapter
            this.layoutManager = notificationLayoutManager
            this.visible(true)
        }
        binding.progressBar.visible(false)
        notificationAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}