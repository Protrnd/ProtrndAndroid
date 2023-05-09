package protrnd.com.ui.wallet.send

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSendAmountBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.viewmodels.PaymentViewModel

class SendAmountFragment :
    BaseFragment<PaymentViewModel, FragmentSendAmountBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        var amount = ""

        binding.amountInput.transformationMethod = null

        binding.amountInput.requestFocus()
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.amountInput, SHOW_IMPLICIT)

        binding.amountInput.addTextChangedListener {
            amount = it.toString()
        }

        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("amount", amount)
            hostFragment.navController.navigate(R.id.sendTypeFragment, bundle)
        }

        binding.quickSendBtn.setOnClickListener {
            binding.quickSendBtn.enable(false)
            val bundle = Bundle()
            bundle.putString("amount", amount)
            imm.hideSoftInputFromWindow(binding.amountInput.windowToken, 0)
            Handler(Looper.getMainLooper()).postDelayed({
                hostFragment.navController.navigate(R.id.QRScannerFragment, bundle)
            }, 500)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSendAmountBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}