package protrnd.com.ui.wallet.withdraw

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
import protrnd.com.databinding.FragmentWithdrawBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.setSpannableColor
import protrnd.com.ui.viewmodels.PaymentViewModel

class WithdrawFragment :
    BaseFragment<PaymentViewModel, FragmentWithdrawBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.enable(false)
        var amount = ""

        binding.amountInput.transformationMethod = null

        binding.amountInput.addTextChangedListener {
            amount = it.toString()
            if (amount.isNotEmpty()) {
                if (amount.toInt() < 100)
                    binding.amountInput.error = "Please input a value above 100"
                else if (amount.toInt() > 1_000_000)
                    binding.amountInput.error = "Please input a value below 1,000,000"
                else
                    binding.continueBtn.enable(true)
            } else {
                binding.continueBtn.enable(false)
            }
        }

        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("amount", amount)
            hostFragment.navController.navigate(R.id.withdrawDetailsFragment, bundle)
        }
        binding.note.text = binding.note.text.toString().setSpannableColor("Note:")
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWithdrawBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        return PaymentRepository(paymentApi)
    }
}