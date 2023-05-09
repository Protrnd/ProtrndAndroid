package protrnd.com.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPromotionPage1Binding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel

class PromotionPage1Fragment :
    BaseFragment<PaymentViewModel, FragmentPromotionPage1Binding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment
        val bottomSheet = hostFragment.parentFragment as PromotionBottomSheet
        requireArguments().putString("postId", bottomSheet.getPostId())
        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(
                R.id.choosePromotionMethodFragment,
                requireArguments()
            )
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPromotionPage1Binding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }

}