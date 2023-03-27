package protrnd.com.ui.promotion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentChoosePromotionMethodBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel

class ChoosePromotionMethodFragment : BaseFragment<PaymentViewModel, FragmentChoosePromotionMethodBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        binding.massPromotion.setOnClickListener {
            hostFragment.navController.navigate(ChoosePromotionMethodFragmentDirections.actionChoosePromotionMethodFragmentToPromotionLocationFragment())
        }

        binding.targetedPromotion.setOnClickListener {
            hostFragment.navController.navigate(ChoosePromotionMethodFragmentDirections.actionChoosePromotionMethodFragmentToPromotionLocationFragment())
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChoosePromotionMethodBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}