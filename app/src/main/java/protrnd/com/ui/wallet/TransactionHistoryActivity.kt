package protrnd.com.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivityTransactionHistoryBinding
import protrnd.com.ui.adapter.TransactionsPagingAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity
import protrnd.com.ui.showTransactionDetails
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.visible

class TransactionHistoryActivity :
    BaseActivity<ActivityTransactionHistoryBinding, PaymentViewModel, PaymentRepository>() {

    lateinit var adapter: TransactionsPagingAdapter

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityTransactionHistoryBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        val transactionDatabase =
            protrndAPIDataSource.provideTransactionDatabase(application)
        return PaymentRepository(paymentApi)
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
        val transactions = MemoryCache.transactionsList
        if (transactions.isNotEmpty())
            adapter.submitData(lifecycle, PagingData.from(transactions))

        viewModel.getTransactionsPage().observe(this) {
            adapter.submitData(lifecycle, it)
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

    override fun onDestroy() {
        super.onDestroy()
        val snapshot = adapter.snapshot()
        if (snapshot.isNotEmpty())
            MemoryCache.transactionsList = snapshot.items.toMutableList()
    }

}