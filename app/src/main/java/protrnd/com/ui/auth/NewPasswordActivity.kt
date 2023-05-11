package protrnd.com.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import protrnd.com.data.models.Login
import protrnd.com.data.models.ResetPasswordDTO
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.ActivityNewPasswordBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.viewmodels.AuthViewModel

class NewPasswordActivity : BaseActivity<ActivityNewPasswordBinding, AuthViewModel, AuthRepository>() {
    override fun getActivityBinding(inflater: LayoutInflater) = ActivityNewPasswordBinding.inflate(inflater)

    override fun getViewModel() = AuthViewModel::class.java

    override fun getActivityRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        isAuth = true

        intent!!

        val otpHash = intent.getStringExtra("hash")!!
        val otpPlain = intent.getStringExtra("plain")!!
        val email = intent.getStringExtra("email")!!

        binding.returnBtn.setOnClickListener {
            finishActivity()
        }

        binding.resetBtn.enable(false)

        var password = ""
        binding.passwordEt.addTextChangedListener {
            password = it.toString()
            binding.resetBtn.enable(binding.passwordEt.isPasswordLongEnough())
        }

        binding.resetBtn.setOnClickListener {
            binding.progressBar.visible(true)
            binding.resetBtn.enable(false)
            binding.passwordEt.enable(false)
            CoroutineScope(Dispatchers.IO).launch {
                when (val reset = viewModel.resetPassword(ResetPasswordDTO(otpHash, otpPlain, Login(email, password)))) {
                    is Resource.Success -> {
                        if (reset.value.successful) {
                            if (authToken == null)
                                startNewActivityWithNoBackstack(AuthenticationActivity::class.java)
                            else
                                startNewActivityWithNoBackstack(HomeActivity::class.java)
                        } else {
                            if (reset.value.statusCode == 403) {
                                binding.root.errorSnackBar("Invalid OTP inserted")
                                finishActivity()
                            }
                            if (reset.value.statusCode == 400) {
                                binding.progressBar.visible(false)
                                binding.resetBtn.enable(true)
                                binding.passwordEt.enable(true)
                                binding.root.errorSnackBar("Error resetting password")
                            }
                        }
                    }
                    is Resource.Failure -> {
                        binding.progressBar.visible(false)
                        binding.resetBtn.enable(true)
                        binding.passwordEt.enable(true)
                        binding.root.errorSnackBar("An Error occurred!")
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visible(true)
                        binding.resetBtn.enable(false)
                        binding.passwordEt.enable(false)
                    }
                }
            }
        }
    }

    private fun EditText.isPasswordLongEnough(): Boolean {
        if (this.text.toString().trim().length >= 7) return true
        this.error = "Your password has to be at-least 7 characters"
        return false
    }

}