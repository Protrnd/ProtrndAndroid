package protrnd.com.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentWalletBinding
import protrnd.com.ui.adapter.TransactionHistoryAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.startAnimation
import protrnd.com.ui.visible
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment
import protrnd.com.ui.wallet.topup.TopUpBottomSheetFragment
import protrnd.com.ui.wallet.withdraw.WithdrawBottomSheetFragment

class WalletFragment : BaseFragment<PaymentViewModel, FragmentWalletBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.transactionHistoryRv.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionHistoryRv.adapter = TransactionHistoryAdapter()
        binding.sendMoneyBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheet = SendMoneyBottomSheetFragment(this)
            bottomSheet.show(childFragmentManager,bottomSheet.tag)
        }
        binding.topupBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheet = TopUpBottomSheetFragment(this)
            bottomSheet.show(childFragmentManager,bottomSheet.tag)
        }
        binding.withdrawBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheet = WithdrawBottomSheetFragment(this)
            bottomSheet.show(childFragmentManager,bottomSheet.tag)
        }
        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(),TransactionHistoryActivity::class.java))
            requireActivity().startAnimation()
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWalletBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }
}