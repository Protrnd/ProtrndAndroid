package protrnd.com.ui.wallet.topup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentTopUpSuccessBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.setGradient

class TopUpSuccessFragment : BaseFragment<PaymentViewModel, FragmentTopUpSuccessBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newBalanceText.setGradient()
        binding.newBalanceValue.setGradient()
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTopUpSuccessBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}