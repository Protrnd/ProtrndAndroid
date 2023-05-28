package protrnd.com.ui.wallet.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSendTypeBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel

class SendTypeFragment :
    BaseFragment<PaymentViewModel, FragmentSendTypeBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val amount = requireArguments().getString("amount").toString()
        val hostFragment = parentFragment as NavHostFragment
        var navDirection: Int = R.id.QRScannerFragment
        binding.protrndScanBtn.isChecked = true
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            navDirection = if (checkedId == R.id.protrnd_id_btn) {
                R.id.enterProfileIDFragment
            } else {
                R.id.QRScannerFragment
            }
        }

        val bundle = Bundle()
        bundle.putString("amount", amount)
        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(navDirection, bundle)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSendTypeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        return PaymentRepository(paymentApi)
    }
}