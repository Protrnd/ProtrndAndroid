package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentSelectAccountTypeBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.setGradient
import protrnd.com.ui.setSpannableBold
import protrnd.com.ui.setSpannableColor

class SelectAccountTypeFragment :
    BaseFragment<AuthViewModel, FragmentSelectAccountTypeBinding, AuthRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHost = parentFragment as NavHostFragment
        val authActivity = fragmentHost.parentFragment as RegisterFragment
        val base = authActivity.requireActivity() as AuthenticationActivity

        binding.loginTv.setGradient()
        binding.loginHereTv.setGradient()
        binding.userAccountBtn.text = binding.userAccountBtn.text.toString().setSpannableBold("Explore")
        binding.enterpriseAccountBtn.text = binding.enterpriseAccountBtn.text.toString().setSpannableBold("Business")
        binding.loginText.text = binding.loginText.text.toString().setSpannableColor("Login", 25)

        binding.userAccountBtn.setOnClickListener {
            authActivity.verifyOTP.registerDto?.accountType = "user"
            Navigation.findNavController(requireView())
                .navigate(SelectAccountTypeFragmentDirections.actionSelectAccountTypeFragmentToInputProfileDetailsFragment())
        }
        binding.loginText.setOnClickListener {
            base.startFragment(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }
        binding.enterpriseAccountBtn.setOnClickListener {
            authActivity.verifyOTP.registerDto?.accountType = "business"
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
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), settingsPreferences)
}