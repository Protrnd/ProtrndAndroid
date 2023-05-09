package protrnd.com.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.NavHostFragment
import protrnd.com.R
import protrnd.com.data.models.VerifyOTP
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.FragmentRegisterBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.finishActivity
import protrnd.com.ui.viewmodels.AuthViewModel

class RegisterFragment : BaseFragment<AuthViewModel, FragmentRegisterBinding, AuthRepository>() {

    var verifyOTP: VerifyOTP = VerifyOTP()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHost =
            childFragmentManager.findFragmentById(R.id.register_fragment_container) as NavHostFragment
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val isSelectAccountType =
                        navHost.childFragmentManager.fragments.any { it.javaClass == SelectAccountTypeFragment::class.java && it.isVisible }
                    if (!isSelectAccountType)
                        navHost.navController.popBackStack()
                    val authActivity = requireActivity() as AuthenticationActivity
                    try {
                        authActivity.startFragment(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
                    } catch (e: Exception) {
                        authActivity.finishActivity()
                    }
                }
            })
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegisterBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}