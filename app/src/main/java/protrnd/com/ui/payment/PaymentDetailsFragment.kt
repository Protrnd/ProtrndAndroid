package protrnd.com.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentDetailsBinding
import protrnd.com.ui.base.BaseFragment

class PaymentDetailsFragment : BaseFragment<PaymentViewModel, FragmentPaymentDetailsBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val from = requireArguments().getString("from")
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            if (from == getString(R.string.top_up))
                hostFragment.navController.navigate(R.id.topUpSuccessFragment)
            else
                hostFragment.navController.navigate(R.id.paymentCompleteFragment)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentDetailsBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}