package protrnd.com.ui.support

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSupportAmountBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.promotion.PromotionLocationFragmentDirections

class SupportAmountFragment : BaseFragment<PaymentViewModel, FragmentSupportAmountBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(SupportAmountFragmentDirections.actionSupportAmountFragmentToChoosePaymentMethodFragment())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSupportAmountBinding.inflate(inflater, container,false)

    override fun getFragmentRepository() = PaymentRepository()
}