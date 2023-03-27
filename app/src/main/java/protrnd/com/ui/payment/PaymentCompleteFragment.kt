package protrnd.com.ui.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPaymentCompleteBinding
import protrnd.com.ui.base.BaseFragment

class PaymentCompleteFragment : BaseFragment<PaymentViewModel, FragmentPaymentCompleteBinding, PaymentRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPaymentCompleteBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}