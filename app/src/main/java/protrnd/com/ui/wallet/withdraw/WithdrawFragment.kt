package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentWithdrawBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.setSpannableColor

class WithdrawFragment : BaseFragment<PaymentViewModel, FragmentWithdrawBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.enable(false)
        binding.amountInput.addTextChangedListener {
            binding.continueBtn.enable(it.toString().isNotEmpty() && it.toString().toInt() > 0)
        }
        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            val amount = binding.amountInput.text.toString()
            bundle.putString("amount",amount)
            hostFragment.navController.navigate(R.id.withdrawDetailsFragment, bundle)
        }
        binding.note.text = binding.note.text.toString().setSpannableColor("Note:")
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWithdrawBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}