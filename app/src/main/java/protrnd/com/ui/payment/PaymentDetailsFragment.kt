package protrnd.com.ui.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.*
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentDetailsBinding
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.chat.ChatContentActivity
import protrnd.com.ui.settings.CardDetailsBottomSheetFragment
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.WalletFragment
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment
import protrnd.com.ui.wallet.topup.TopUpBottomSheetFragment

class PaymentDetailsFragment :
    BaseFragment<PaymentViewModel, FragmentPaymentDetailsBinding, PaymentRepository>() {
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""
    private var from = ""
    private var amount = 0

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)
        var buttonClicked = false
        val hostFragment = parentFragment as NavHostFragment
        if (hostFragment.parentFragment !is CardDetailsBottomSheetFragment) {
            from = requireArguments().getString("from", "")
            amount = requireArguments().getInt("amount", 0)
        }

        if (requireArguments().getString("from","") == "profileid") {
            from = "profileid"
        }

        CoroutineScope(Dispatchers.IO).launch {
            val savedCard = profilePreferences.cardData.first()
            if (savedCard != null) {
                val cardData = Gson().fromJson(savedCard, CardData::class.java)
                binding.cvv.setText(cardData.cvv)
                binding.cardNumber.setText(cardData.cardNo)
                binding.expiryDate.setText(cardData.expiry)
                binding.holderName.setText(cardData.holder)
            }
        }

        NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) { networkAvailable ->
            if (!buttonClicked)
                binding.continueBtn.enable(networkAvailable)
            Toast.makeText(requireContext(), if (networkAvailable) "Network available" else "You are disconnected!", Toast.LENGTH_SHORT).show()
        }

        var bannerUrl = ""
        var plan = ""
        var locationDTO = LocationDTO()

        if (from == getString(R.string.promotion)) {
            bannerUrl = requireArguments().getString("bannerurl")!!
            plan = requireArguments().getString("plan")!!
            val location = requireArguments().getString("location")!!
            val locationSplit = location.split(",")

            locationDTO = when (locationSplit.size) {
                3 -> LocationDTO(city = locationSplit[2], state = locationSplit[1])
                2 -> LocationDTO(state = locationSplit[1])
                else -> LocationDTO()
            }
        }

        PaystackSdk.initialize(requireContext())
        PaystackSdk.setPublicKey("pk_live_e53a78c4d3e8865414294fa2c62e5e1fd720a21f")

        amount *= 100

        binding.cardNumber.addTextChangedListener {
            checkInputFields()
        }

        binding.expiryDate.addTextChangedListener {
            if (it.toString().length == 2 && !it.toString().contains("/")) {
                it!!.append("/")
            }
            if (it.toString().length == 5)
                binding.cvv.requestFocus()

            checkInputFields()
        }

        binding.cvv.addTextChangedListener {
            if (it.toString().length == 3)
                binding.cvv.clearFocus()

            checkInputFields()
        }

        val pinBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setCanceledOnTouchOutside(false)
        pinBottomSheet.setContentView(pinFrag.root)
        pinBottomSheet.setOnDismissListener {
            binding.progressBar.visible(false)
            binding.continueBtn.enable(true)
            pinFrag.continueBtn.enable(true)
            binding.root.enable(true)
        }
        pinBottomSheet.setOnCancelListener {
            binding.progressBar.visible(false)
            binding.continueBtn.enable(true)
            pinFrag.continueBtn.enable(true)
            binding.root.enable(true)
        }
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)
        binding.continueBtn.setOnClickListener {
            if (requireActivity().isNetworkAvailable()) {
                buttonClicked = true
                binding.progressBar.visible(true)
                if (binding.cvv.text.isNotEmpty() && binding.expiryDate.text.isNotEmpty() && binding.cardNumber.text.isNotEmpty() && binding.holderName.text.isNotEmpty()) {
                    val cardExpiry = binding.expiryDate.text.toString().trim()
                    val cardNumber = binding.cardNumber.text.toString().trim()
                    val holder = binding.holderName.text.toString().trim()
                    val cvv = binding.cvv.text.toString().trim()
                    val cardExpiryArray = cardExpiry.split("/").toTypedArray()
                    val expiryMonth = cardExpiryArray[0].toInt()
                    val expiryYear = cardExpiryArray[1].toInt()

                    binding.root.enable(false)

                    //here you can check for network availability first, if the network is available, continue
                    pinBottomSheet.show()
                    pinFrag.continueBtn.setOnClickListener {
                        pinBottomSheet.setCancelable(false)
                        otp1 = pinFrag.input1.text.toString()
                        otp2 = pinFrag.input2.text.toString()
                        otp3 = pinFrag.input3.text.toString()
                        otp4 = pinFrag.input4.text.toString()
                        pinFrag.continueBtn.enable(false)
                        if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
                            val pin = "$otp1$otp2$otp3$otp4"
                            lifecycleScope.launch {
                                when (val pinRequest = viewModel.isPaymentPinCorrect(pin)) {
                                    is Resource.Success -> {
                                        if (pinRequest.value.data) {
                                            if (amount == 0) {
                                                val cardBottomSheet =
                                                    hostFragment.parentFragment as CardDetailsBottomSheetFragment
                                                profilePreferences.saveCardDetails(
                                                    CardData(
                                                        holder = holder,
                                                        cardNumber,
                                                        cardExpiry,
                                                        cvv
                                                    )
                                                )
                                                cardBottomSheet.dismiss()
                                                pinBottomSheet.dismiss()
                                            } else {
                                                if (binding.rememberCardDetails.isChecked)
                                                    profilePreferences.saveCardDetails(
                                                        CardData(
                                                            holder = holder,
                                                            cardNumber,
                                                            cardExpiry,
                                                            cvv
                                                        )
                                                    )
                                                pinBottomSheet.dismiss()
                                                if (requireActivity().isNetworkAvailable()) {
                                                    val card = Card(
                                                        cardNumber,
                                                        expiryMonth,
                                                        expiryYear,
                                                        cvv
                                                    )
                                                    val charge = Charge()
                                                    charge.putCustomField(
                                                        "card holder name",
                                                        holder
                                                    )
                                                    charge.putCustomField("receiver","protrnd.com")
                                                    charge.amount = amount
                                                    charge.email = currentUserProfile.email
                                                    charge.card = card
                                                    PaystackSdk.chargeCard(
                                                        requireActivity(),
                                                        charge,
                                                        object : Paystack.TransactionCallback {
                                                            override fun onSuccess(transaction: Transaction) {
                                                                requireArguments().putInt(
                                                                    "amount",
                                                                    amount / 100
                                                                )

                                                                if (from == "profileid") {
                                                                    val bundle = requireArguments().getParcelableBundle<QrCodeContent>("content")
                                                                    val isChat = requireArguments().getBoolean("isChat", false)
                                                                    val sendBottomFrag = hostFragment.parentFragment as SendMoneyBottomSheetFragment
                                                                    if (bundle != null) {
                                                                        lifecycleScope.launch {
                                                                            val send =
                                                                                viewModel.sendProtrndFunds(
                                                                                    FundsDTO(
                                                                                        amount = bundle.amount.toDouble(),
                                                                                        profileid = bundle.profile.id,
                                                                                        fromid = currentUserProfile.id,
                                                                                        reference = generateRef()
                                                                                    )
                                                                                )
                                                                            val sendData = send.data!!
                                                                            if (sendData.successful) {
                                                                                if (isChat && sendBottomFrag.activity is ChatContentActivity) {
                                                                                    val dto =
                                                                                        ChatDTO(
                                                                                            message = "Support",
                                                                                            receiverid = bundle.profile.id,
                                                                                            type = "payment",
                                                                                            itemid = "${sendData.data}",
                                                                                            convoid = sendBottomFrag.convoid
                                                                                        )
                                                                                    withContext(Dispatchers.Main) {
                                                                                        sendBottomFrag.activity.addNewMessage(
                                                                                            dto
                                                                                        )
                                                                                        hostFragment.navController.navigate(
                                                                                            R.id.sendSuccessFragment,
                                                                                            requireArguments()
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    withContext(Dispatchers.Main) {
                                                                                        hostFragment.navController.navigate(
                                                                                            R.id.sendSuccessFragment,
                                                                                            requireArguments()
                                                                                        )
                                                                                        val wallet =
                                                                                            sendBottomFrag.parentFragment
                                                                                        if (wallet is WalletFragment) {
                                                                                            wallet.updateBalance()
                                                                                        }
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                withContext(Dispatchers.Main) {
                                                                                    binding.root.errorSnackBar(
                                                                                        "Error sending payment"
                                                                                    )
                                                                                    binding.continueBtn.enable(
                                                                                        true
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                if (from == getString(R.string.promotion)) {
                                                                    val promotionDto = PromotionDTO(
                                                                        amount / 100,
                                                                        bannerUrl,
                                                                        plan,
                                                                        currentUserProfile.email,
                                                                        profileId = currentUserProfile.id,
                                                                        audience = locationDTO,
                                                                        postId = requireArguments().getString(
                                                                            "postId"
                                                                        )!!
                                                                    )
                                                                    lifecycleScope.launch {
                                                                        val verify =
                                                                            viewModel.verifyPromotionPayment(
                                                                                VerifyPromotion(
                                                                                    reference = transaction.reference,
                                                                                    promotion = promotionDto
                                                                                )
                                                                            )
                                                                        if (verify.data!!.successful) {
                                                                            hostFragment.navController.navigate(
                                                                                R.id.paymentCompleteFragment,
                                                                                requireArguments()
                                                                            )
                                                                        } else {
                                                                            Toast.makeText(
                                                                                requireContext(),
                                                                                "An Error occurred, if you were charged, please send us a mail at protrndng@gmail.com \nLet the subject be ${transaction.reference}",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                        }
                                                                    }
                                                                }

                                                                if (from == getString(R.string.top_up)) {
                                                                    lifecycleScope.launch {
                                                                        val topup =
                                                                            viewModel.topUpFunds(
                                                                                FundsDTO(
                                                                                    (amount / 100).toDouble(),
                                                                                    fromid = currentUserProfile.id,
                                                                                    profileid = currentUserProfile.id,
                                                                                    reference = transaction.reference
                                                                                )
                                                                            )
                                                                        if (topup.data!!.successful) {
                                                                            val bs =
                                                                                hostFragment.parentFragment as TopUpBottomSheetFragment
                                                                            val wallet =
                                                                                bs.parentFragment as WalletFragment
                                                                            wallet.updateBalance()
                                                                            hostFragment.navController.navigate(
                                                                                R.id.topUpSuccessFragment,
                                                                                requireArguments()
                                                                            )
                                                                        } else {
                                                                            binding.root.errorSnackBar("Error making top up")
                                                                        }
                                                                    }
                                                                }

                                                                if (from == getString(R.string.support)) {
                                                                    val supportBottomSheet =
                                                                        hostFragment.parentFragment as SupportBottomSheet
                                                                    val post =
                                                                        supportBottomSheet.getPostValue()
                                                                    val supportDto = SupportDTO(
                                                                        amount = amount / 100,
                                                                        receiverId = post.profileid,
                                                                        postId = post.id,
                                                                        reference = transaction.reference
                                                                    )

                                                                    lifecycleScope.launch {
                                                                        val verify =
                                                                            viewModel.supportPost(
                                                                                supportDto
                                                                            )
                                                                        if (verify.data!!.successful) {
                                                                            hostFragment.navController.navigate(
                                                                                R.id.paymentCompleteFragment,
                                                                                requireArguments()
                                                                            )
                                                                        } else {
                                                                            Toast.makeText(
                                                                                requireContext(),
                                                                                "An Error occurred, if you were charged, please send us a mail at protrndng@gmail.com \nLet the subject be ${transaction.reference}",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            override fun beforeValidate(transaction: Transaction?) {
                                                            }

                                                            override fun onError(
                                                                error: Throwable,
                                                                transaction: Transaction?
                                                            ) {
                                                                Toast.makeText(
                                                                    requireContext(),
                                                                    "Transaction error please try again",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                binding.root.enable(true)
                                                                binding.progressBar.visible(false)
                                                            }
                                                        })
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Please check your internet",
                                                        Toast.LENGTH_LONG
                                                    )
                                                        .show()
                                                    binding.progressBar.visible(false)
                                                    binding.root.enable(true)
                                                }
                                            }
                                        } else {
                                            pinBottomSheet.dismiss()
                                            binding.root.errorSnackBar("Invalid payment pin")
                                        }
                                    }
                                    is Resource.Failure -> {
                                        binding.root.enable(true)
                                        pinFrag.continueBtn.enable(true)
                                        pinBottomSheet.dismiss()
                                        Toast.makeText(
                                            requireContext(),
                                            "Invalid payment pin",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                    pinBottomSheet.show()
                } else {
                    binding.root.errorSnackBar("OTP is incomplete")
                }
            } else {
                binding.root.errorSnackBar("Please check your network connection!")
            }
        }
    }

    private fun checkInputFields() {
        if (binding.cvv.text.toString().length == 3 && binding.cardNumber.text.toString().length == 16 && binding.expiryDate.length() == 5 && binding.holderName.text.toString()
                .isNotEmpty()
        )
            binding.continueBtn.enable(true)
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }
}