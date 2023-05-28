package protrnd.com.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentCompleteBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.promotion.PromotionBottomSheet
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.PaymentViewModel

class PaymentCompleteFragment :
    BaseFragment<PaymentViewModel, FragmentPaymentCompleteBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val from = requireArguments().getString("from")
        binding.continueBtn.setOnClickListener {
            if (from == getString(R.string.support)) {
                val supportBottomSheet = hostFragment.parentFragment as SupportBottomSheet
                supportBottomSheet.dismiss()
            } else if (from == getString(R.string.promotion)) {
                val promoBottomSheet = hostFragment.parentFragment as PromotionBottomSheet
                promoBottomSheet.dismiss()
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentCompleteBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        return PaymentRepository(paymentApi)
    }
}