package protrnd.com.ui.promotion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPromotionPage1Binding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeFragment
import protrnd.com.ui.payment.PaymentViewModel

class PromotionPage1Fragment : BaseFragment<PaymentViewModel, FragmentPromotionPage1Binding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(PromotionPage1FragmentDirections.actionPromotionPage1FragmentToChoosePromotionMethodFragment())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPromotionPage1Binding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()

}