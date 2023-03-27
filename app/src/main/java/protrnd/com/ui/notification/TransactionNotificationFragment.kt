package protrnd.com.ui.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.NotificationApi
import protrnd.com.data.repository.NotificationRepository
import protrnd.com.databinding.FragmentTransactionNotificationBinding
import protrnd.com.ui.base.BaseFragment

class TransactionNotificationFragment : BaseFragment<NotificationViewModel, FragmentTransactionNotificationBinding, NotificationRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun getViewModel() = NotificationViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionNotificationBinding.inflate(inflater,container,false)

    override fun getFragmentRepository(): NotificationRepository {
        val token = runBlocking { settingsPreferences.authToken.first() }
        val api = ProtrndAPIDataSource().buildAPI(NotificationApi::class.java, token)
        val db = ProtrndAPIDataSource().provideNotificationDatabase(requireActivity().application)
        val profileDb = ProtrndAPIDataSource().provideProfileDatabase(requireActivity().application)
        return NotificationRepository(api, db, profileDb)
    }
}