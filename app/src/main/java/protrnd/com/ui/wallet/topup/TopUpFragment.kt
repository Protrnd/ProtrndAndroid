package protrnd.com.ui.wallet.topup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentTopUpBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel

class TopUpFragment : BaseFragment<PaymentViewModel, FragmentTopUpBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val from = Bundle()
        from.putString("from",getString(R.string.top_up))

        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(R.id.paymentDetailsFragment, from)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTopUpBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}