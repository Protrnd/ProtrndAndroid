package protrnd.com.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivityTransactionHistoryBinding
import protrnd.com.databinding.TransactionDetailsLayoutBinding
import protrnd.com.ui.adapter.TransactionHistoryAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.visible

class TransactionHistoryActivity : BaseActivity<ActivityTransactionHistoryBinding, PaymentViewModel, PaymentRepository>() {
    override fun getActivityBinding(inflater: LayoutInflater) = ActivityTransactionHistoryBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository() = PaymentRepository()

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val actionBar = supportActionBar!!
        actionBar.title = "Transaction History"
        binding.toolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)
        val adapter = TransactionHistoryAdapter()
        binding.transactionsRv.layoutManager = LinearLayoutManager(this)
        binding.transactionsRv.adapter = adapter
        adapter.click(object : TransactionItemListener {
            override fun click() {
                binding.alphaBg.visible(true)
                val bottomSheetDialog = BottomSheetDialog(this@TransactionHistoryActivity, R.style.BottomSheetTheme)
                val bottomSheet = TransactionDetailsLayoutBinding.inflate(layoutInflater)
                bottomSheetDialog.setContentView(bottomSheet.root)
                bottomSheetDialog.show()
                bottomSheetDialog.setOnDismissListener {
                    binding.alphaBg.visible(false)
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
             android.R.id.home -> {
                 finishActivity()
             }
         }
        return true
    }
}