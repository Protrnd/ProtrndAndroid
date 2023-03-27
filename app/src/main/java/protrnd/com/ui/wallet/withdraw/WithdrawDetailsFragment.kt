package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentWithdrawDetailsBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel

class WithdrawDetailsFragment : BaseFragment<PaymentViewModel, FragmentWithdrawDetailsBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val amount = requireArguments().getString("amount").toString()
        binding.continueBtn.text = "Withdraw ₦$amount"
        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(R.id.withdrawSuccessFragment)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWithdrawDetailsBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}