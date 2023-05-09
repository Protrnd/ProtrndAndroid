package protrnd.com.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentTransactionNotificationBinding
import protrnd.com.ui.adapter.TransactionsPagingAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.showTransactionDetails
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.visible

class TransactionNotificationFragment :
    BaseFragment<PaymentViewModel, FragmentTransactionNotificationBinding, PaymentRepository>() {

    lateinit var adapter: TransactionsPagingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionsPagingAdapter(viewModel, viewLifecycleOwner, currentUserProfile)

        binding.root.setOnRefreshListener {
            getTransactions()
        }

        binding.transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecycler.adapter = adapter

        getTransactions()

        adapter.click(object : TransactionItemListener {
            override fun click(transaction: Transaction) {
                binding.alphaBg.visible(true)
                lifecycleScope.launch {
                    showTransactionDetails(
                        requireContext(),
                        layoutInflater,
                        transaction,
                        currentUserProfile,
                        binding.alphaBg,
                        viewModel,
                        viewLifecycleOwner
                    )
                }
            }
        })
    }

    private fun getTransactions() {
        val transactions = MemoryCache.transactionsList
        if (transactions.isNotEmpty())
            adapter.submitData(lifecycle, PagingData.from(transactions))

        viewModel.getTransactionsPage().observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
    }


    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionNotificationBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val transactionsApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(transactionsApi)
    }

    override fun onDestroy() {
        super.onDestroy()
        val snapshot = adapter.snapshot()
        if (snapshot.isNotEmpty())
            MemoryCache.transactionsList = snapshot.items.toMutableList()
    }
}