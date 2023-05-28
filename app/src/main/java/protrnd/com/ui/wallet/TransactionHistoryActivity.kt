package protrnd.com.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.backgroundtask.SaveMessagesService
import protrnd.com.data.network.backgroundtask.SaveTransactionsService
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivityTransactionHistoryBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.TransactionsPagingAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.viewmodels.PaymentViewModel

class TransactionHistoryActivity :
    BaseActivity<ActivityTransactionHistoryBinding, PaymentViewModel, PaymentRepository>() {

    lateinit var adapter: TransactionsPagingAdapter

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityTransactionHistoryBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val datasource = ProtrndAPIDataSource()
        val paymentApi = datasource.buildAPI(PaymentApi::class.java, token)
        val db = datasource.provideTransactionDatabase(application)
        val profileDb = datasource.provideProfileDatabase(application)
        return PaymentRepository(paymentApi,db,profileDb)
    }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val actionBar = supportActionBar!!
        binding.toolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)
        adapter = TransactionsPagingAdapter(viewModel, this, currentUserProfile)
        binding.transactionsRv.layoutManager = LinearLayoutManager(this)
        binding.transactionsRv.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            getTransactions()
        }

        getTransactions()

        adapter.click(object : TransactionItemListener {
            override fun click(transaction: Transaction) {
                binding.alphaBg.visible(true)
                lifecycleScope.launch {
                    showTransactionDetails(
                        this@TransactionHistoryActivity,
                        layoutInflater,
                        transaction,
                        currentUserProfile,
                        binding.alphaBg,
                        viewModel,
                        this@TransactionHistoryActivity
                    )
                }
            }
        })
    }

    fun getTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = viewModel.getAllTransactions()?.first()
            if (transactions != null && transactions.isNotEmpty()) {
                adapter.submitData(lifecycle, PagingData.from(transactions))
                binding.transactionEmpty.root.visible(false)
            }
            delay(2000)
            withContext(Dispatchers.Main) {
                viewModel.getTransactionsPage().observe(this@TransactionHistoryActivity) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishActivity()
            }
        }
        return true
    }

    override fun onStop() {
        if (adapter.snapshot().items.isNotEmpty()) {
            val snapshot = if (adapter.snapshot().items.size > 20) adapter.snapshot().items.subList(0, 20) else adapter.snapshot().items
            val data = Data.Builder()
                .putString("transactions", Gson().toJson(snapshot))
                .build()

            SaveTransactionsService.setApplication(application)
            val worker = OneTimeWorkRequest.Builder(SaveTransactionsService::class.java)
                .setInputData(data)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build()
                )
                .build()
            WorkManager.getInstance(this).enqueue(worker)
        }
        super.onStop()
    }
}