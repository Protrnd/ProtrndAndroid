package protrnd.com.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentLoginBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.AuthViewModel

class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding, AuthRepository>() {
    var email = ""
    var password = ""

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)
        binding.loginBtn.enable(false)
        val authActivity = activity as AuthenticationActivity

        binding.signupHereTv.setOnClickListener {
            authActivity.startFragment(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.forgotPass.setOnClickListener {
            startActivity(Intent(authActivity, ForgotPasswordActivity::class.java).apply {
                authActivity.startAnimation()
            })
        }

        binding.loginTv.setGradient()
        binding.signupHereTv.text =
            binding.signupHereTv.text.toString().setSpannableColor("Sign up here", 74)

        viewModel.loginResponse.observe(viewLifecycleOwner) {
            binding.progressBar.visible(it is Resource.Loading)
            binding.loginBtn.enable(true)
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.visible(true)
                    binding.loginBtn.enable(false)
                }
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    if(it.value.statusCode == SUCCESS_CODE) {
                        saveAndStartHomeFragment(
                            it.value.data.toString(),
                            lifecycleScope,
                            requireActivity(),
                            profilePreferences
                        )
                    } else {
                        binding.root.errorSnackBar("Invalid email or password, please try again")
                    }
                }
                is Resource.Failure -> {
                    if (it.isNetworkError)
                        binding.root.errorSnackBar("Please check your network connection")
                    else {
                        binding.progressBar.visible(false)
                        binding.loginBtn.enable(true)
                        binding.root.errorSnackBar("Invalid email or password")
                    }
                }
                else -> {}
            }
        }

        binding.emailEt.addTextChangedListener {
            email = binding.emailEt.text.toString().trim()
            if (isValidEmail(email)) {
                binding.loginBtn.enable(
                    email.isNotEmpty() && binding.passwordEt.toString().isNotEmpty()
                )
            } else {
                binding.emailEt.error = "Invalid email address"
            }
        }

        binding.passwordEt.addTextChangedListener {
            email = binding.emailEt.text.toString().trim()
            if (isValidEmail(email))
                binding.loginBtn.enable(email.isNotEmpty() && it.toString().isNotEmpty())
            else
                binding
        }

        binding.loginBtn.setOnClickListener {
            email = binding.emailEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            binding.progressBar.visible(true)
            NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
                login()
            }
        }
    }

    private fun login() {
        viewModel.login(email, password)
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}