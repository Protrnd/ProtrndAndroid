package protrnd.com.ui.wallet.send

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentProfileResultBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.viewmodels.PaymentViewModel

class ProfileResultFragment :
    BaseFragment<PaymentViewModel, FragmentProfileResultBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("content", QrCodeContent::class.java)
        } else
            requireArguments().getParcelable("content")

        val sendResult = bundle!!
        val username = "@${sendResult.profile.username}"
        binding.profileIdResult.text = username
        val sendAmount = "Send ₦${sendResult.amount.formatAmount()}"
        binding.continueBtn.text = sendAmount

        binding.profileName.text = sendResult.profile.fullname
        binding.username.text = username
        binding.location.text = sendResult.profile.location

        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(R.id.confirmSendFragment, requireArguments())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileResultBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}