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
import protrnd.com.databinding.FragmentPaymentPlanBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatAmount
import protrnd.com.ui.viewmodels.PaymentViewModel

class PaymentPlanFragment :
    BaseFragment<PaymentViewModel, FragmentPaymentPlanBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentDetails = requireArguments()
        val amount = paymentDetails.getInt("amount")
        val location = paymentDetails.getString("location")!!
        val monthlyAmount = amount * 3

        val weeklyPlan =
            "1 week\n₦ ${amount.formatAmount()}\nRun promotions for 7 days to all users in $location"
        val monthlyPlan =
            "1 month\n₦ ${monthlyAmount.formatAmount()}\nRun promotions for 1 month to all users in $location"

        binding.monthlyPlan.text = monthlyPlan
        binding.weeklyPlan.text = weeklyPlan

        val hostFragment = parentFragment as NavHostFragment

        binding.continueBtn.setOnClickListener {
            if (binding.monthlyPlan.isChecked) {
                requireArguments().putInt("amount", monthlyAmount)
                requireArguments().putString("plan", "month")
            } else {
                requireArguments().putInt("amount", amount)
                requireArguments().putString("plan", "week")
            }
            hostFragment.navController.navigate(
                R.id.promotionSubscriptionDetailsFragment,
                requireArguments()
            )
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentPlanBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}