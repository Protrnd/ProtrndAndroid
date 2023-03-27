package protrnd.com.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPromotionLocationBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel
import protrnd.com.ui.visible

class PromotionLocationFragment : BaseFragment<PaymentViewModel, FragmentPromotionLocationBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.basic.isChecked = true
        binding.targetArea.visible(false)
        binding.stateArea.visible(false)
        binding.cityArea.visible(false)

        binding.statePicker.selectItemByIndex(0)
        binding.cityPicker.selectItemByIndex(0)

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.basic -> {
                    binding.targetArea.visible(false)
                    binding.stateArea.visible(false)
                    binding.cityArea.visible(false)
                }
                else -> {
                    binding.areaPicker.clearSelectedItem()
                    binding.targetArea.visible(true)
                }
            }
        }

        var total = 10000
        val country = "Nigeria"
        var state = "Rivers"
        var city ="Port Harcourt"

        binding.areaPicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            if (newItem == "State") {
                binding.stateArea.visible(true)
                binding.cityArea.visible(false)
                total = 5000
            }
            if (newItem == "City") {
                binding.stateArea.visible(true)
                binding.cityArea.visible(true)
                total = 2000
            }
            if (newItem == "Country") {
                binding.stateArea.visible(false)
                binding.cityArea.visible(false)
                total = 10000
            }
        }

        binding.cityPicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            city = newItem
        }

        binding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            state = newItem
        }

        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            val location = when(binding.areaPicker.selectedIndex)  {
                2 -> "$country, $state, $city"
                1 -> "$country, $state"
                else -> country
            }
            val bundle = Bundle()
            bundle.putString("location",location)
            bundle.putInt("amount",total)
            hostFragment.navController.navigate(R.id.paymentPlanFragment,bundle)
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPromotionLocationBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}