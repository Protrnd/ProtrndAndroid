package protrnd.com.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivityCheckoutBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.viewmodels.PaymentViewModel

class CheckoutActivity :
    BaseActivity<ActivityCheckoutBinding, PaymentViewModel, PaymentRepository>() {
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityCheckoutBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(application)
        return PaymentRepository(paymentApi)
    }
}