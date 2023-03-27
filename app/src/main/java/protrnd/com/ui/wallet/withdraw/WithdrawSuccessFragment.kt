package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentWithdrawSuccessBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.setGradient

class WithdrawSuccessFragment : BaseFragment<PaymentViewModel, FragmentWithdrawSuccessBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newBalanceText.setGradient()
        binding.newBalanceValue.setGradient()
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWithdrawSuccessBinding.inflate(inflater, container,false)

    override fun getFragmentRepository() = PaymentRepository()
}