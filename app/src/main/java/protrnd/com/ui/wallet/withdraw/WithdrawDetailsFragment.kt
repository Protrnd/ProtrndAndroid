package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.AccountDTO
import protrnd.com.data.models.WithdrawDTO
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.databinding.FragmentWithdrawDetailsBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.WalletFragment

class WithdrawDetailsFragment :
    BaseFragment<PaymentViewModel, FragmentWithdrawDetailsBinding, PaymentRepository>() {
    private var bankName = ""
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var buttonClicked = false
        val hostFragment = parentFragment as NavHostFragment
        val amount = requireArguments().getString("amount").toString().toInt()
        val withdrawText = "Withdraw ₦${amount.formatAmount()}"
        binding.continueBtn.text = withdrawText

        NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) { networkAvailable ->
            if (!buttonClicked)
                binding.continueBtn.enable(networkAvailable)
            Toast.makeText(requireContext(), if (networkAvailable) "Network available" else "You are disconnected!", Toast.LENGTH_SHORT).show()
        }

        binding.accountNumber.transformationMethod = null

        binding.continueBtn.enable(false)

        binding.bankName.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            bankName = newItem
            binding.continueBtn.enable(
                (binding.accountNumber.text.toString().length >= 10) && (binding.accountName.text.toString()
                    .trim().isNotEmpty()) && (bankName.isNotEmpty())
            )
        }

        binding.accountNumber.addTextChangedListener {
            binding.continueBtn.enable(
                (it.toString().length >= 10) && (binding.accountName.text.toString().trim()
                    .isNotEmpty()) && (bankName.isNotEmpty())
            )
        }

        binding.accountName.addTextChangedListener {
            binding.continueBtn.enable(
                (it.toString().length >= 10) && (binding.accountNumber.text.toString().trim()
                    .isNotEmpty()) && (bankName.isNotEmpty())
            )
        }

        val pinBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setContentView(pinFrag.root)
        pinBottomSheet.setCancelable(false)
        pinBottomSheet.setCanceledOnTouchOutside(false)
        pinBottomSheet.setOnDismissListener {
            binding.root.enable(true)
            pinFrag.progressBar.visible(false)
            pinFrag.continueBtn.enable(true)
        }
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)
        binding.continueBtn.setOnClickListener {
            if (requireActivity().isNetworkAvailable()) {
                buttonClicked = true
                binding.continueBtn.enable(false)
                binding.root.enable(false)
                pinBottomSheet.show()
                pinFrag.continueBtn.setOnClickListener {
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
                                        viewModel.withdrawFunds(
                                            WithdrawDTO(
                                                amount = amount, account = AccountDTO(
                                                    accountName = binding.accountName.text.toString()
                                                        .trim(),
                                                    accountNumber = binding.accountNumber.text.toString()
                                                        .trim(),
                                                    bankName = bankName,
                                                    profileId = currentUserProfile.id
                                                )
                                            )
                                        )
                                        viewModel._withdraw.observe(viewLifecycleOwner) {
                                            when (it) {
                                                is Resource.Success -> {
                                                    val wallet =
                                                        hostFragment.requireParentFragment().parentFragment as WalletFragment
                                                    wallet.updateBalance()
                                                    requireArguments().putInt("amount", amount)
                                                    hostFragment.navController.navigate(
                                                        R.id.withdrawSuccessFragment,
                                                        requireArguments()
                                                    )
                                                }
                                                is Resource.Loading -> binding.continueBtn.enable(
                                                    false
                                                )
                                                else -> binding.continueBtn.enable(true)
                                            }
                                        }
                                    } else {
                                        pinBottomSheet.dismiss()
                                    }
                                }
                                is Resource.Failure -> {
                                    binding.root.enable(true)
                                    pinFrag.continueBtn.enable(true)
                                    Toast.makeText(
                                        requireContext(),
                                        "Invalid payment pin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                is Resource.Loading -> {
                                    pinFrag.progressBar.visible(true)
                                }
                            }
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
    ) = FragmentWithdrawDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }
}