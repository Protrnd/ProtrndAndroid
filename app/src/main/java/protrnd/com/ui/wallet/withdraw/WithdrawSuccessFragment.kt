package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentWithdrawSuccessBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.setGradient
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.WalletFragment

class WithdrawSuccessFragment :
    BaseFragment<PaymentViewModel, FragmentWithdrawSuccessBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment
        val withdrawBottomSheetFragment = hostFragment.parentFragment as WithdrawBottomSheetFragment
        val wallet = withdrawBottomSheetFragment.parentFragment as WalletFragment

        binding.newBalanceText.setGradient()
        binding.newBalanceValue.setGradient()

        viewModel.getBalance(currentUserProfile.id)

        viewModel._balance.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val result =
                        "${wallet.balance + requireArguments().getInt("amount")}".formatAmount()
                    val displayValue =
                        if (result.contains(".00")) result.replace(".00", "") else result
                    binding.fromAmount.text = if (displayValue == "") "₦0" else "₦$displayValue"
                    binding.newBalanceText.setGradient()
                    binding.newBalanceValue.setGradient()

                    val newResult = "${it.value.data}".formatAmount()
                    val newDisplayValue =
                        if (newResult.contains(".00")) newResult.replace(".00", "") else newResult
                    binding.newBalanceValue.text = newDisplayValue

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
            withdrawBottomSheetFragment.dismiss()
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWithdrawSuccessBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}