package protrnd.com.ui.payment

import android.os.Bundle
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.*
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentDetailsBinding
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.errorSnackBar
import protrnd.com.ui.isNetworkAvailable
import protrnd.com.ui.requestForFocus
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.WalletFragment
import protrnd.com.ui.wallet.topup.TopUpBottomSheetFragment

class PaymentDetailsFragment :
    BaseFragment<PaymentViewModel, FragmentPaymentDetailsBinding, PaymentRepository>() {
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)

        val from = requireArguments().getString("from")!!
        var amount = requireArguments().getInt("amount", 0)

        val savedCard = runBlocking { profilePreferences.cardData.first() }

        if (savedCard != null) {
            val cardData = Gson().fromJson(savedCard, CardData::class.java)
            binding.cvv.setText(cardData.cvv)
            binding.cardNumber.setText(cardData.cardNo)
            binding.expiryDate.setText(cardData.expiry)
            binding.holderName.setText(cardData.holder)
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
        PaystackSdk.setPublicKey("pk_test_2e5cd2f5435099fd111779f7bbcdd4d66d7194f3")
        amount *= 100
        val hostFragment = parentFragment as NavHostFragment

        binding.cardNumber.addTextChangedListener {
            if (it.toString().length == 16)
                binding.expiryDate.requestFocus()
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
        pinBottomSheet.setOnCancelListener {
            binding.root.enable(true)
        }
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)
        binding.continueBtn.setOnClickListener {
            if (binding.cvv.text.isNotEmpty() && binding.expiryDate.text.isNotEmpty() && binding.cardNumber.text.isNotEmpty() && binding.holderName.text.isNotEmpty()) {
                val cardExpiry = binding.expiryDate.text.toString().trim()
                val cardNumber = binding.cardNumber.text.toString().trim()
                val holder = binding.holderName.text.toString().trim()
                val cvv = binding.cvv.text.toString().trim()
                val cardExpiryArray = cardExpiry.split("/").toTypedArray()
                val expiryMonth = cardExpiryArray[0].toInt()
                val expiryYear = cardExpiryArray[1].toInt()

                binding.root.enable(false)

                val card = Card(cardNumber, expiryMonth, expiryYear, cvv)
                val charge = Charge()
                charge.putCustomField("card holder name", holder)
                charge.amount = amount
                charge.email = currentUserProfile.email
                charge.card = card
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
                                            PaystackSdk.chargeCard(
                                                requireActivity(),
                                                charge,
                                                object : Paystack.TransactionCallback {
                                                    override fun onSuccess(transaction: Transaction) {
                                                        requireArguments().putInt(
                                                            "amount",
                                                            amount / 100
                                                        )
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
                                                                val topup = viewModel.topUpFunds(
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
                                                                    viewModel.supportPost(supportDto)
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
                                                    }
                                                })
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Please check your internet",
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                            binding.root.enable(true)
                                        }
                                    } else {
                                        pinBottomSheet.dismiss()
                                        binding.root.enable(true)
                                        pinFrag.continueBtn.enable(true)
                                        binding.root.errorSnackBar("Invalid payment pin")
                                    }
                                }
                                is Resource.Failure -> {
                                    pinBottomSheet.dismiss()
                                    binding.root.enable(true)
                                    pinFrag.continueBtn.enable(true)
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
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}