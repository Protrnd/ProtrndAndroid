package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import protrnd.com.data.network.AuthApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentLoginBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeActivity

class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding, AuthRepository>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.enable(false)

        binding.signupHereTv.setOnClickListener {
            val authActivity = activity as AuthenticationActivity
            authActivity.startFragment(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.signupHereTv.text = binding.signupHereTv.text.toString().setSpannableColor("Sign up here",74)

        viewModel.loginResponse.observe(viewLifecycleOwner) {
            binding.progressBar.visible(it is Resource.Loading)
            binding.loginBtn.enable(true)
            when (it) {
                is Resource.Loading ->{
                    binding.progressBar.visible(true)
                    binding.loginBtn.enable(false)
                }
                is Resource.Success -> {
                    lifecycleScope.launch {
                        viewModel.saveAuthToken(it.value.data.toString())
                        requireActivity().startNewActivityFromAuth(HomeActivity::class.java)
                    }
                }
                is Resource.Failure -> handleAPIError(it)
            }
        }

        binding.passwordEt.addTextChangedListener {
            val email = binding.emailEt.text.toString().trim()
            binding.loginBtn.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (isValidEmail(email)) {
                binding.loginBtn.enable(false)
                viewModel.login(email, password)
            } else {
                binding.emailEt.error = "Invalid email format"
            }
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}