package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch
import protrnd.com.R
import protrnd.com.data.network.AuthApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentInputProfileDetailsBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment

class InputProfileDetailsFragment :
    BaseFragment<AuthViewModel, FragmentInputProfileDetailsBinding, AuthRepository>() {
    private lateinit var registerFragment: RegisterFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHost = parentFragment as NavHostFragment
        registerFragment = fragmentHost.parentFragment as RegisterFragment
        binding.continueBtn.enable(false)
        val base = registerFragment.requireActivity() as AuthenticationActivity
        binding.loginHereTv.setOnClickListener {
            base.startFragment(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        binding.loginHereTv.text = binding.loginHereTv.text.toString().setSpannableColor("Login here",72)

        if (registerFragment.verifyOTP.registerDto?.accountType == "business") {
            binding.detailsHeader.text = getString(R.string.business_details)
            binding.nameTv.text = getString(R.string.business_fullname)
            binding.usernameTv.text = getString(R.string.business_username_at)
        } else {
            binding.detailsHeader.text = getString(R.string.signup)
            binding.nameTv.text = getString(R.string.fullname)
            binding.usernameTv.text = getString(R.string.username_with_at)
        }

        viewModel.registerResponse.observe(viewLifecycleOwner){
            when(it) {
                is Resource.Success -> {
                    if (it.value.successful) {
                        registerFragment.verifyOTP.otpHash = it.value.data.toString()
                        Navigation.findNavController(requireView())
                            .navigate(InputProfileDetailsFragmentDirections.actionInputProfileDetailsFragmentToVerifyOTPFragment())
                    } else {
                        if (it.value.statusCode == 400) {
                            binding.root.snackbar(it.value.message)
                        }
                    }
                    binding.continueBtn.enable(true)
                }
                is Resource.Loading -> {
                    binding.continueBtn.enable(false)
                }
                is Resource.Failure -> {
                    this.handleAPIError(it){ lifecycleScope.launch { register() } }
                }
            }
        }

        binding.emailEt.addTextChangedListener {
            if (binding.emailEt.inputNotEmpty() &&
                binding.usernameEt.inputNotEmpty() && binding.nameEt.inputNotEmpty() && binding.passwordEt.inputNotEmpty()
            ) {
                binding.continueBtn.enable(true)
            }
        }

        binding.nameEt.addTextChangedListener {
            if (binding.emailEt.inputNotEmpty() &&
                binding.usernameEt.inputNotEmpty() && binding.nameEt.inputNotEmpty() && binding.passwordEt.inputNotEmpty()
            ) {
                binding.continueBtn.enable(true)
            }
        }

        binding.passwordEt.addTextChangedListener {
            if (binding.emailEt.inputNotEmpty() &&
                binding.usernameEt.inputNotEmpty() && binding.nameEt.inputNotEmpty() && binding.passwordEt.inputNotEmpty()
            ) {
                binding.continueBtn.enable(true)
            }
        }

        binding.usernameEt.addTextChangedListener {
            if (binding.emailEt.inputNotEmpty() &&
                binding.usernameEt.inputNotEmpty() && binding.nameEt.inputNotEmpty() && binding.passwordEt.inputNotEmpty()
            ) {
                binding.continueBtn.enable(true)
            }
        }

        binding.continueBtn.setOnClickListener {
            if (binding.emailEt.inputNotEmpty() &&
                binding.usernameEt.inputNotEmpty() && binding.nameEt.inputNotEmpty() && binding.passwordEt.inputNotEmpty()
            ) {
                if (!isValidEmail(binding.emailEt.text.toString().trim())) {
                    binding.emailIl.error = "Please input a valid email"
                } else if (!binding.usernameEt.nonAlphabeticCharacterNotExists()) {
                    binding.usernameEt.error = "Please remove all non characters"
                } else {
                    if (binding.passwordEt.isPasswordLongEnough())  {
                        registerFragment.verifyOTP.registerDto?.fullName = binding.nameEt.text.toString().trim()
                        registerFragment.verifyOTP.registerDto?.userName = binding.usernameEt.text.toString().trim()
                        registerFragment.verifyOTP.registerDto?.email = binding.emailEt.text.toString().trim()
                        registerFragment.verifyOTP.registerDto?.password = binding.passwordEt.text.toString().trim()
                        binding.progressBar.visible(true)
                        lifecycleScope.launch {
                            register()
                        }
                    }
                }
            }
        }
    }

    private suspend fun register() {
        viewModel.register(registerFragment.verifyOTP.registerDto!!)
    }

    private fun EditText.nonAlphabeticCharacterNotExists(): Boolean {
        return this.text.toString().trim().matches("^[a-zA-Z]*$".toRegex())
    }

    private fun EditText.isPasswordLongEnough(): Boolean {
        if (this.text.toString().trim().length >= 7)
            return true
        this.error = "Your password has to be at-least 7 characters"
        return false
    }

    private fun EditText.inputNotEmpty(): Boolean {
        if (this.text.toString().trim().isNotEmpty())
            return true
        this.error = "Please Fill"
        return false
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInputProfileDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}