package protrnd.com.ui.promotion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentPlanBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.payment.PaymentViewModel

class PaymentPlanFragment : BaseFragment<PaymentViewModel, FragmentPaymentPlanBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentDetails = requireArguments()
        val amount = paymentDetails.getInt("amount")
        val location = paymentDetails.getString("location")!!
        val monthlyAmount = amount *3

        val weeklyPlan = "1 week\n₦ ${amount.formatAmount()}\nRun promotions for 7 days to all users in $location"
        val monthlyPlan = "1 month\n₦ ${monthlyAmount.formatAmount()}\nRun promotions for 1 month to all users in $location"

        binding.monthlyPlan.text = monthlyPlan
        binding.weeklyPlan.text = weeklyPlan

        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            val bundle = Bundle()
            if (binding.monthlyPlan.isChecked) {
                bundle.putInt("amount", monthlyAmount)
                bundle.putString("plan","month")
            }
            else {
                bundle.putInt("amount", amount)
                bundle.putString("plan","week")
            }
            bundle.putString("location",location)
            hostFragment.navController.navigate(R.id.promotionSubscriptionDetailsFragment,bundle)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentPlanBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}