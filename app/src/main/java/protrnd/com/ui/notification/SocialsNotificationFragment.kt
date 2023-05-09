package protrnd.com.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.FragmentSocialsNotificationBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.NotificationViewModel

class SocialsNotificationFragment :
    BaseFragment<NotificationViewModel, FragmentSocialsNotificationBinding, NotificationRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun getViewModel() = NotificationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSocialsNotificationBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NotificationRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = ProtrndAPIDataSource().buildAPI(NotificationApi::class.java, token)
        val db = ProtrndAPIDataSource().provideNotificationDatabase(requireActivity().application)
        val profileDb = ProtrndAPIDataSource().provideProfileDatabase(requireActivity().application)
        return NotificationRepository(api, db, profileDb)
    }
}