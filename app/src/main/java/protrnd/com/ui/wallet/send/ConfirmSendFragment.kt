package protrnd.com.ui.wallet.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.models.FundsDTO
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.data.responses.BooleanResponseBody
import protrnd.com.databinding.FragmentConfirmSendBinding
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.chat.ChatContentActivity
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.WalletFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmSendFragment :
    BaseFragment<PaymentViewModel, FragmentConfirmSendBinding, PaymentRepository>() {

    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment
        val sendBottomFrag = hostFragment.parentFragment as SendMoneyBottomSheetFragment

        val bundle = requireArguments().getParcelableBundle<QrCodeContent>("content")
        var buttonClicked = false
        val sendResult = bundle!!
        val username = "@${sendResult.profile.username}"
        val sendAmount = "Send ₦${sendResult.amount.formatAmount()}"
        val sendTo = "Sending ₦${sendResult.amount.formatAmount()} to"
        val isChat = requireArguments().getBoolean("isChat", false)

        NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) { networkAvailable ->
            if (!buttonClicked)
                binding.continueBtn.enable(networkAvailable)
            Toast.makeText(requireContext(), if (networkAvailable) "Network available" else "You are disconnected!", Toast.LENGTH_SHORT).show()
        }

        binding.continueBtn.text = sendAmount
        binding.sendAmount.text =
            sendTo.setSpannableColor("₦${sendResult.amount.formatAmount()}", "Sending ".length)

        binding.profileName.text = sendResult.profile.fullname
        binding.username.text = username
        binding.location.text = sendResult.profile.location

        val pinBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setContentView(pinFrag.root)
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)

        pinBottomSheet.setOnDismissListener {
            binding.root.enable(true)
            binding.continueBtn.enable(true)
            binding.progressBar.visible(false)
            pinFrag.progressBar.visible(false)
            pinFrag.continueBtn.enable(true)
        }

        pinBottomSheet.setOnCancelListener {
            binding.root.enable(true)
            binding.continueBtn.enable(true)
            binding.progressBar.visible(false)
            pinFrag.progressBar.visible(false)
            pinFrag.continueBtn.enable(true)
        }

        binding.continueBtn.setOnClickListener {
            if (requireActivity().isNetworkAvailable()) {
                buttonClicked = true
                binding.continueBtn.enable(false)
                pinBottomSheet.show()
                pinFrag.continueBtn.setOnClickListener {
                    pinFrag.continueBtn.enable(false)
                    pinFrag.progressBar.visible(true)
                    otp1 = pinFrag.input1.text.toString()
                    otp2 = pinFrag.input2.text.toString()
                    otp3 = pinFrag.input3.text.toString()
                    otp4 = pinFrag.input4.text.toString()
                    if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
                        val pin = "$otp1$otp2$otp3$otp4"
                        lifecycleScope.launch {
                            when (val pinRequest = viewModel.isPaymentPinCorrect(pin)) {
                                is Resource.Success -> {
                                    if (pinRequest.value.data) {
                                        pinBottomSheet.dismiss()
                                        val send = viewModel.sendProtrndFunds(
                                            FundsDTO(
                                                amount = sendResult.amount.toDouble(),
                                                profileid = sendResult.profile.id,
                                                fromid = currentUserProfile.id,
                                                reference = generateRef()
                                            )
                                        )
                                        val sendData = send.data!!
                                        if (sendData.successful) {
                                            if (isChat && sendBottomFrag.activity is ChatContentActivity) {
                                                val dto = ChatDTO(
                                                    message = "Support",
                                                    receiverid = sendResult.profile.id,
                                                    type = "payment",
                                                    itemid = "${sendData.data}",
                                                    convoid = sendBottomFrag.convoid
                                                )
                                                sendBottomFrag.activity.addNewMessage(dto)
                                                hostFragment.navController.navigate(
                                                    R.id.sendSuccessFragment,
                                                    requireArguments()
                                                )
                                            } else {
                                                hostFragment.navController.navigate(
                                                    R.id.sendSuccessFragment,
                                                    requireArguments()
                                                )
                                                val wallet = sendBottomFrag.parentFragment
                                                if (wallet is WalletFragment) {
                                                    wallet.updateBalance()
                                                }
                                            }
                                        } else {
                                            binding.root.errorSnackBar("Error sending payment")
                                            binding.continueBtn.enable(true)
                                        }
                                    } else {
                                        pinFrag.root.errorSnackBar("Invalid payment pin")
                                        pinBottomSheet.dismiss()
                                    }
                                }
                                is Resource.Loading -> {
                                    pinFrag.progressBar.visible(true)
                                }
                                is Resource.Failure -> {
                                    pinBottomSheet.dismiss()
                                    pinFrag.root.errorSnackBar("Error occurred!")
                                }
                            }
                        }
                    }
                }
            } else {
                binding.root.errorSnackBar("Please check your network connection")
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConfirmSendBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }
}