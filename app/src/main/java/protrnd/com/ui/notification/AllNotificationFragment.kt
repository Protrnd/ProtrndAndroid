package protrnd.com.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.FragmentAllNotificationBinding
import protrnd.com.ui.RecyclerViewReadyCallback
import protrnd.com.ui.adapter.NotificationAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.NotificationViewModel

class AllNotificationFragment :
    BaseFragment<NotificationViewModel, FragmentAllNotificationBinding, NotificationRepository>() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        notificationAdapter = NotificationAdapter(
            viewModel = viewModel,
            activity = requireActivity(),
            lifecycleOwner = this
        )

        val recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
                    setupRecyclerView()
                    loadMoreItems()
                }
            }
        }

        binding.notificationRecycler.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerViewReadyCallback.onLayoutReady()
                binding.notificationRecycler.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun loadMoreItems() {
        val cacheNotifications = MemoryCache.allNotifications
        if (cacheNotifications.isNotEmpty())
            notificationAdapter.submitData(lifecycle, PagingData.from(cacheNotifications))

        viewModel.getNotificationsPage().observe(viewLifecycleOwner) {
            notificationAdapter.submitData(lifecycle, it)
        }
    }

    private fun setupRecyclerView() {
        binding.notificationRecycler.apply {
            this.adapter = notificationAdapter
            this.layoutManager = notificationLayoutManager
        }
    }

    override fun getViewModel() = NotificationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAllNotificationBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NotificationRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = ProtrndAPIDataSource().buildAPI(NotificationApi::class.java, token)
        val db = ProtrndAPIDataSource().provideNotificationDatabase(requireActivity().application)
        val profileDb = ProtrndAPIDataSource().provideProfileDatabase(requireActivity().application)
        return NotificationRepository(api, db, profileDb)
    }

    override fun onPause() {
        super.onPause()
        val snapshot = notificationAdapter.snapshot()
        if (snapshot.isNotEmpty())
            MemoryCache.allNotifications = snapshot.items.toMutableList()
    }
}