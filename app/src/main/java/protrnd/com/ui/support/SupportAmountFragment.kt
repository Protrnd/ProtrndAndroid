package protrnd.com.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSupportAmountBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.viewmodels.PaymentViewModel

class SupportAmountFragment :
    BaseFragment<PaymentViewModel, FragmentSupportAmountBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.enable(false)
        binding.amountEt.addTextChangedListener {
            val amount = it.toString()
            if (amount.contains("."))
                amount.replace(".", "")
            else {
                if (amount.toInt() < 100)
                    binding.amountEt.error = "Amount cannot be less than 100"
                else if (amount.toInt() > 1_000_000)
                    binding.amountEt.error = "Amount cannot be greater than 1,000,000"
                else
                    binding.continueBtn.enable(true)
            }
        }

        binding.continueBtn.setOnClickListener {
            val value = binding.amountEt.text.toString().toInt()
            requireArguments().putInt("amount", value)
            requireArguments().putString("from", getString(R.string.support))
            hostFragment.navController.navigate(
                R.id.chooseSupportPaymentMethodFragment,
                requireArguments()
            )
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSupportAmountBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        return PaymentRepository(paymentApi)
    }
}