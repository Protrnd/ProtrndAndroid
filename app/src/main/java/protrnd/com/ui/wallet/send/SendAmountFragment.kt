package protrnd.com.ui.wallet.send

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSendAmountBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.payment.PaymentViewModel

class SendAmountFragment : BaseFragment<PaymentViewModel, FragmentSendAmountBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.enable(false)

        binding.amountInput.addTextChangedListener {
            binding.continueBtn.enable(it.toString().isNotEmpty() && it.toString().toInt() > 0)
        }

        binding.continueBtn.setOnClickListener {
            val amount = binding.amountInput.text.toString()
            val bundle = Bundle()
            bundle.putString("amount",amount)
            hostFragment.navController.navigate(R.id.sendTypeFragment,bundle)
        }

        binding.quickSendBtn.setOnClickListener {
            hostFragment.navController.navigate(SendAmountFragmentDirections.actionSendAmountFragmentToQRScannerFragment())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSendAmountBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}