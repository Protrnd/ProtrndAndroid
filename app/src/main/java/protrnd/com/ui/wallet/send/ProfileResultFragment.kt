package protrnd.com.ui.wallet.send

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentProfileResultBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel

class ProfileResultFragment : BaseFragment<PaymentViewModel,FragmentProfileResultBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sendAmount = "Send ₦${requireArguments().getString("amount").toString()}"
        binding.continueBtn.text = sendAmount
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileResultBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}