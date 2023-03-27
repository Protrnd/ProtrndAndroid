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
import protrnd.com.databinding.FragmentEnterProfileIDBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.payment.PaymentViewModel

class EnterProfileIDFragment : BaseFragment<PaymentViewModel, FragmentEnterProfileIDBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHost = parentFragment as NavHostFragment
        binding.continueBtn.enable(false)
        val sendAmount = "Send ₦${requireArguments().getString("amount").toString().trim()}"
        binding.continueBtn.text = sendAmount
        binding.prodileIdInput.addTextChangedListener {
            binding.continueBtn.enable(it.toString().isNotEmpty())
        }

        val bundle = Bundle()
        bundle.putString("amount",requireArguments().getString("amount").toString())
        binding.continueBtn.setOnClickListener {
            navHost.navController.navigate(R.id.profileResultFragment,bundle)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEnterProfileIDBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}