package protrnd.com.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentChoosePaymentMethodBinding
import protrnd.com.ui.base.BaseFragment

class ChoosePaymentMethodFragment : BaseFragment<PaymentViewModel, FragmentChoosePaymentMethodBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(ChoosePaymentMethodFragmentDirections.actionChooseSupportPaymentMethodFragmentToSupportPaymentDetailsFragment())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChoosePaymentMethodBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}