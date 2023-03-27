package protrnd.com.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPromotionSubscriptionDetailsBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.payment.PaymentViewModel

class PromotionSubscriptionDetailsFragment : BaseFragment<PaymentViewModel,FragmentPromotionSubscriptionDetailsBinding,PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        val period = arguments.getString("plan")
        val location = arguments.getString("location")
        val plan = "1 $period"
        binding.plan.text = plan
        val amount = "₦ ${arguments.getInt("amount").formatAmount()} / $period"
        binding.amount.text = amount
        binding.locationS.text = location
        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            hostFragment.navController.navigate(R.id.promotionBannerFragment)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPromotionSubscriptionDetailsBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}