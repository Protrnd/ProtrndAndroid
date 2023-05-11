package protrnd.com.ui.payment

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
import protrnd.com.data.models.*
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPayFromWalletBalanceBinding
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.PaymentViewModel

class PayFromWalletBalanceFragment :
    BaseFragment<PaymentViewModel, FragmentPayFromWalletBalanceBinding, PaymentRepository>() {
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""
    var amount = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        viewModel._balance.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    amount = it.value.data
                    requireArguments().putString("balance", "$amount")
                    val result = "$amount".formatAmount()
                    val displayValue =
                        if (result.contains(".00")) result.replace(".00", "") else result
                    binding.totalBalanceValue.text =
                        if (displayValue == "") "₦0" else "₦$displayValue"
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

        viewModel.getBalance(currentUserProfile.id)

        val from = requireArguments().getString("from")!!
        val amount = requireArguments().getInt("amount", 0)

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
        } else {
            val supportAmount = "Support with ₦${amount.formatAmount()}"
            binding.btnMakePayment.text = supportAmount
        }

        val ref = generateRef()

        val pinBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setCanceledOnTouchOutside(false)
        pinBottomSheet.setContentView(pinFrag.root)
        pinBottomSheet.setOnCancelListener {
            binding.root.enable(true)
        }
        pinBottomSheet.setOnDismissListener {
            binding.root.enable(true)
        }

        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)

        binding.btnMakePayment.setOnClickListener {
            binding.root.enable(false)
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
                                    pinBottomSheet.dismiss()

                                    if (from == getString(R.string.promotion)) {
                                        val promotionDto = PromotionDTO(
                                            amount,
                                            bannerUrl,
                                            plan,
                                            currentUserProfile.email,
                                            profileId = currentUserProfile.id,
                                            audience = locationDTO,
                                            postId = requireArguments().getString("postId")!!
                                        )
                                        lifecycleScope.launch {
                                            val verify = viewModel.verifyPromotionPayment(
                                                VerifyPromotion(
                                                    reference = ref,
                                                    promotion = promotionDto
                                                )
                                            )
                                            if (verify.data!!.successful) {
                                                hostFragment.navController.navigate(
                                                    R.id.paymentCompleteFragment,
                                                    requireArguments()
                                                )
                                            } else {
                                                binding.root.enable(true)
                                                Toast.makeText(
                                                    requireContext(),
                                                    "An Error occurred, if you were charged, please send us a mail at protrndng@gmail.com \nLet the subject be $ref",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }

                                    if (from == getString(R.string.support)) {
                                        val supportBottomSheet =
                                            hostFragment.parentFragment as SupportBottomSheet
                                        val post = supportBottomSheet.getPostValue()
                                        val supportDto = SupportDTO(
                                            amount = amount,
                                            receiverId = post.profileid,
                                            postId = post.id,
                                            reference = ref
                                        )

                                        lifecycleScope.launch {
                                            val verify =
                                                viewModel.virtualMoneySupportPost(supportDto)
                                            if (verify.data!!.successful) {
                                                hostFragment.navController.navigate(
                                                    R.id.paymentCompleteFragment,
                                                    requireArguments()
                                                )
                                            } else {
                                                binding.root.enable(true)
                                                Toast.makeText(
                                                    requireContext(),
                                                    "An Error occurred, if you were charged, please send us a mail at protrndng@gmail.com \nLet the subject be $ref",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
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
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPayFromWalletBalanceBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }

}