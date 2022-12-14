package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.network.AuthApi
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentSelectAccountTypeBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.setSpannableColor

class SelectAccountTypeFragment :
    BaseFragment<AuthViewModel, FragmentSelectAccountTypeBinding, AuthRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.continueBtn.enable(false)
        val fragmentHost = parentFragment as NavHostFragment
        val authActivity = fragmentHost.parentFragment as RegisterFragment
        val base = authActivity.requireActivity() as AuthenticationActivity
        binding.loginHereTv.setOnClickListener {
            base.startFragment(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.continueBtn.enable(true)
            when (checkedId) {
                R.id.user_account_btn -> {
                    authActivity.verifyOTP.registerDto?.accountType = "user"
                }
                R.id.enterprise_account_btn -> {
                    authActivity.verifyOTP.registerDto?.accountType = "business"
                }
            }
        }

        binding.loginHereTv.text =
            binding.loginHereTv.text.toString().setSpannableColor("Login here", 72)

        binding.continueBtn.setOnClickListener {
            Navigation.findNavController(requireView())
                .navigate(SelectAccountTypeFragmentDirections.actionSelectAccountTypeFragmentToInputProfileDetailsFragment())
        }
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectAccountTypeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}