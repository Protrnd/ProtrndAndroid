package protrnd.com.ui.wallet.send

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentSendSuccessBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.setGradient
import protrnd.com.ui.setSpannableColor
import protrnd.com.ui.viewmodels.PaymentViewModel

class SendSuccessFragment :
    BaseFragment<PaymentViewModel, FragmentSendSuccessBinding, PaymentRepository>() {
    var balance = 0.0

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSendSuccessBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment
        val sendMoneyBottomSheetFragment =
            hostFragment.parentFragment as SendMoneyBottomSheetFragment

        val content = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("content", QrCodeContent::class.java)
        } else
            requireArguments().getParcelable("content")

        content!!
        val sentAmountText =
            "You sent ₦${content.amount.formatAmount()} to @${content.profile.username}"
        binding.amountSent.text = sentAmountText.setSpannableColor(
            "@${content.profile.username}",
            "You sent ₦${content.amount.formatAmount()} to ".length
        )
        viewModel.getBalance(currentUserProfile.id)

        viewModel._balance.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    try {
                        balance = it.value.data + content.amount
                        val result = "$balance".formatAmount()
                        val displayValue =
                            if (result.contains(".00")) result.replace(".00", "") else result
                        binding.oldBalance.text = if (displayValue == "") "₦0" else "₦$displayValue"
                        binding.newBalanceText.setGradient()
                        binding.newBalanceValue.setGradient()

                        val newResult = "${balance - content.amount}".formatAmount()
                        val newDisplayValue = if (newResult.contains(".00")) newResult.replace(
                            ".00",
                            ""
                        ) else newResult
                        binding.newBalanceValue.text = newDisplayValue
                    } catch (e: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Error occurred!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        it.error!!.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }

        binding.doneBtn.setOnClickListener {
            sendMoneyBottomSheetFragment.dismiss()
        }
    }
}