package protrnd.com.ui.wallet.topup

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
import protrnd.com.databinding.FragmentTopUpBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.viewmodels.PaymentViewModel

class TopUpFragment : BaseFragment<PaymentViewModel, FragmentTopUpBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val from = Bundle()
        from.putString("from", getString(R.string.top_up))

        binding.amountInput.transformationMethod = null

        var amount = ""
        binding.continueBtn.enable(false)
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

        binding.continueBtn.setOnClickListener {
            from.putInt("amount", amount.toInt())
            hostFragment.navController.navigate(R.id.paymentDetailsFragment, from)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTopUpBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}