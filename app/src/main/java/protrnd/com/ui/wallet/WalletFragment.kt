package protrnd.com.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.databinding.FragmentWalletBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.TransactionsPagingAdapter
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel
import protrnd.com.ui.wallet.receive.ReceiveBottomSheetFragment
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment
import protrnd.com.ui.wallet.topup.TopUpBottomSheetFragment
import protrnd.com.ui.wallet.withdraw.WithdrawBottomSheetFragment

class WalletFragment : BaseFragment<PaymentViewModel, FragmentWalletBinding, PaymentRepository>() {

    lateinit var adapter: TransactionsPagingAdapter
    var balance = 0.0
    private val localBalance = MutableLiveData<Double>()
    private val localBalanceLive: LiveData<Double> = localBalance
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TransactionsPagingAdapter(viewModel, viewLifecycleOwner, currentUserProfile)
        binding.transactionHistoryRv.layoutManager = LinearLayoutManager(requireContext())

        binding.refreshLayout.setOnRefreshListener {
            loadTransactions()
        }

        binding.topupBtn.enable(false)
        binding.withdrawBtn.enable(false)
        binding.sendMoneyBtn.enable(false)

        binding.refreshLayout.isRefreshing = true

        viewModel.pinAvailable.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        paymentPin = it.value.data.toString()
                        profilePreferences.savePaymentPin(it.value.data.toString())
                    }
                }
                else -> {}
            }
        }

        binding.sendMoneyBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            if (isPaymentPinAvailable()) {
                val bottomSheet = SendMoneyBottomSheetFragment(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }

        binding.topupBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            if (isPaymentPinAvailable()) {
                val bottomSheet = TopUpBottomSheetFragment(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }

        binding.withdrawBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            if (isPaymentPinAvailable()) {
                val bottomSheet = WithdrawBottomSheetFragment(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }

        binding.receiveMoneyBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            if (isPaymentPinAvailable()) {
                val bottomSheet = ReceiveBottomSheetFragment(this)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
        }

        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionHistoryActivity::class.java))
            requireActivity().startAnimation()
        }

        viewModel.transactions.observe(viewLifecycleOwner) { value ->
            adapter.submitData(
                viewLifecycleOwner.lifecycle,
                PagingData.from(value)
            )

            binding.transactionHistoryRv.adapter = adapter

            adapter.click(object : TransactionItemListener {
                override fun click(transaction: Transaction) {
                    binding.alphaBg.visible(true)
                    showTransactionDetails(
                        requireContext(),
                        layoutInflater,
                        transaction,
                        currentUserProfile,
                        binding.alphaBg,
                        viewModel, viewLifecycleOwner
                    )
                }
            })

            showRecycler(value.isNotEmpty())
        }

        localBalanceLive.observe(viewLifecycleOwner) {
            val result = "$it".formatAmount()
            val displayValue = if (result.contains(".00")) result.replace(".00", "") else result
            binding.totalBalanceValue.text = if (displayValue == "") "₦0" else "₦$displayValue"

            binding.topupBtn.enable(requireActivity().isNetworkAvailable())
            binding.withdrawBtn.enable(requireActivity().isNetworkAvailable())
            binding.sendMoneyBtn.enable(requireActivity().isNetworkAvailable())

            if (binding.refreshLayout.isRefreshing)
                binding.refreshLayout.isRefreshing = false
        }

        viewModel._balance.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    balance = it.value.data
                    MemoryCache.balance[currentUserProfile.id] = balance
                    localBalance.postValue(balance)
                }
                is Resource.Failure -> {
                    val balance = MemoryCache.balance[currentUserProfile.id]
                    if (balance != null) {
                        val balanceResult: Double = balance
                        localBalance.postValue(balanceResult)
                    }
                    if (binding.refreshLayout.isRefreshing)
                        binding.refreshLayout.isRefreshing = false
                    binding.root.errorSnackBar("Error getting your wallet balance")
                }
                is Resource.Loading -> {
                    val balance = MemoryCache.balance[currentUserProfile.id]
                    if (balance != null) {
                        val balanceResult: Double = balance
                        localBalance.postValue(balanceResult)
                    }
                }
            }
        }

        if (!requireActivity().isNetworkAvailable()) {
            NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) { available ->
                if (available)
                    loadTransactions()
            }
        }
    }

    private fun isPaymentPinAvailable(): Boolean {
        val pinBottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setContentView(pinFrag.root)
        pinBottomSheet.setOnDismissListener {
            removeAlphaVisibility()
        }
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)
        if (paymentPin.isEmpty() || paymentPin == false.toString()) {
            pinBottomSheet.show()
            pinFrag.continueBtn.setOnClickListener {
                pinFrag.continueBtn.enable(false)
                otp1 = pinFrag.input1.text.toString()
                otp2 = pinFrag.input2.text.toString()
                otp3 = pinFrag.input3.text.toString()
                otp4 = pinFrag.input4.text.toString()
                if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
                    val pin = "$otp1$otp2$otp3$otp4"
                    lifecycleScope.launch {
                        when (val pinRequest = viewModel.setPaymentPin(pin)) {
                            is Resource.Success -> {
                                if (pinRequest.value.successful) {
                                    val pinValue = "${pinRequest.value.data}"
                                    if (pinValue == pin) {
                                        profilePreferences.savePaymentPin(pin)
                                        paymentPin = pin
                                        pinBottomSheet.dismiss()
                                        binding.alphaBg.visible(false)
                                    }
                                }
                            }
                            is Resource.Failure -> {
                                Toast.makeText(
                                    requireContext(),
                                    "There was a network error, please try again!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                pinFrag.continueBtn.enable(true)
                            }
                            else -> {}
                        }
                    }
                }
            }
            return false
        } else {
            return true
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    fun updateBalance() {
        loadTransactions()
    }

    private fun showRecycler(state: Boolean) {
        binding.transactionHistoryRv.visible(state)
        binding.transactionEmpty.visible(!state)
    }

    private fun loadTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getTransactionsPage(1)
            viewModel.getBalance(currentUserProfile.id)
            viewModel.isPinAvailable()
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWalletBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }
}