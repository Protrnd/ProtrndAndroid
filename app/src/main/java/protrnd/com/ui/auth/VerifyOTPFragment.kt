package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentVerifyOtpBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment

class VerifyOTPFragment : BaseFragment<AuthViewModel, FragmentVerifyOtpBinding, AuthRepository>() {

    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""
    private lateinit var registerFragment: RegisterFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHost = parentFragment as NavHostFragment
        registerFragment = fragmentHost.parentFragment as RegisterFragment
        binding.input1.requestForFocus(binding.input2)
        binding.input2.requestForFocus(binding.input3, binding.input1)
        binding.input3.requestForFocus(binding.input4, binding.input2)
        binding.input4.requestForFocus(prev = binding.input3)
        binding.enterOtpTv.text =
            binding.enterOtpTv.text.toString().setSpannableColor("One-Time-Password", 10)
        viewModel.verifyOtpResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.successful) {
                        viewModel.saveAndStartHomeFragment(
                            it.value.data.toString(),
                            lifecycleScope,
                            viewLifecycleOwner,
                            requireActivity(),
                            settingsPreferences
                        )
                    } else {
                        binding.root.snackbar(it.value.message)
                    }
                }
                is Resource.Loading -> {
                    binding.continueBtn.enable(false)
                    binding.progressBar.visible(true)
                }
                is Resource.Failure -> {
                    binding.continueBtn.enable(true)
                    binding.progressBar.visible(false)
                    if (it.isNetworkError) {
                        binding.root.snackbar("Error verifying otp, please wait while we try again") { lifecycleScope.launch { verifyOtp() } }
                    } else {
                        binding.root.snackbar("Internal server error occurred")
                    }
                }
                else -> {}
            }
        }

        binding.continueBtn.setOnClickListener {
            otp1 = binding.input1.text.toString()
            otp2 = binding.input2.text.toString()
            otp3 = binding.input3.text.toString()
            otp4 = binding.input4.text.toString()
            if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
                registerFragment.verifyOTP.plainText = "$otp1$otp2$otp3$otp4"
                lifecycleScope.launch {
                    verifyOtp()
                }
            }
        }
    }

    private suspend fun verifyOtp() {
        viewModel.verifyOtp(registerFragment.verifyOTP)
    }

    private fun EditText.requestForFocus(next: EditText? = null, prev: EditText? = null) {
        this.addTextChangedListener {
            if (it.toString().length == 1 && next != null) {
                next.requestFocus()
            } else {
                if (it.toString().isEmpty())
                    prev?.requestFocus()
            }
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentVerifyOtpBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), settingsPreferences)
}