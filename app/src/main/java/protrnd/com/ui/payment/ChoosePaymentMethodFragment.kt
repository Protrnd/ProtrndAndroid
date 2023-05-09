package protrnd.com.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentChoosePaymentMethodBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel

class ChoosePaymentMethodFragment :
    BaseFragment<PaymentViewModel, FragmentChoosePaymentMethodBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            if (binding.cdc.isChecked)
                hostFragment.navController.navigate(R.id.paymentDetailsFragment, requireArguments())
            else
                hostFragment.navController.navigate(
                    R.id.payFromWalletBalanceFragment,
                    requireArguments()
                )
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChoosePaymentMethodBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}