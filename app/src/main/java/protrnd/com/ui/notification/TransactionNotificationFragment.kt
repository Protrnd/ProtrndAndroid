package protrnd.com.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.backgroundtask.SaveTransactionsService
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentTransactionNotificationBinding
import protrnd.com.ui.adapter.TransactionsPagingAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.errorSnackBar
import protrnd.com.ui.isNetworkAvailable
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
            if (requireActivity().isNetworkAvailable())
                getTransactions()
            else {
                binding.root.errorSnackBar("Please check your network connection")
                binding.root.isRefreshing = if (binding.root.isRefreshing) false else false
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = viewModel.getAllTransactions()?.first()
            if (transactions != null && transactions.isNotEmpty()) {
                adapter.submitData(lifecycle, PagingData.from(transactions))
                binding.transactionEmpty.root.visible(false)
            }
            delay(2000)
            withContext(Dispatchers.Main) {
                NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
                    viewModel.getTransactionsPage().observe(viewLifecycleOwner) {
                        lifecycleScope.launch {
                            adapter.loadStateFlow.collectLatest { loadStates ->
                                binding.transactionEmpty.root.visible(adapter.itemCount > 0)
                                if (loadStates.refresh is LoadState.Loading) {
                                    binding.root.isRefreshing = true
                                } else {
                                    binding.root.isRefreshing = false
                                    if (adapter.itemCount < 1) {
                                        binding.transactionEmpty.root.visible(true)
                                    }
                                }
                            }
                        }
                        adapter.submitData(lifecycle, it)
                    }
                }
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionNotificationBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val datasource = ProtrndAPIDataSource()
        val paymentApi = datasource.buildAPI(PaymentApi::class.java, token)
        val db = datasource.provideTransactionDatabase(requireActivity().application)
        val profileDb = datasource.provideProfileDatabase(requireActivity().application)
        return PaymentRepository(paymentApi,db,profileDb)
    }

    override fun onStop() {
        if (adapter.snapshot().items.isNotEmpty()) {
            val snapshot = if (adapter.snapshot().items.size > 20) adapter.snapshot().items.subList(
                0,
                20
            ) else adapter.snapshot().items
            val data = Data.Builder()
                .putString("transactions", Gson().toJson(snapshot))
                .build()

            SaveTransactionsService.setApplication(requireActivity().application)
            val worker = OneTimeWorkRequest.Builder(SaveTransactionsService::class.java)
                .setInputData(data)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build()
                )
                .build()
            WorkManager.getInstance(requireContext()).enqueue(worker)
        }
        super.onStop()
    }
}