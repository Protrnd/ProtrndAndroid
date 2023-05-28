package protrnd.com.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.ActivityForgotPasswordBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.otp.BaseBottomSheetFragment
import protrnd.com.ui.viewmodels.AuthViewModel
import java.util.regex.Pattern

class ForgotPasswordActivity :
    BaseActivity<ActivityForgotPasswordBinding, AuthViewModel, AuthRepository>() {

    private val otpHashDataMutable = MutableLiveData<String>()
    private val otpLive: LiveData<String> = otpHashDataMutable
    val otpInputMutable = MutableLiveData<String>()
    private val otpInputLive: LiveData<String> = otpInputMutable
    var hash = ""

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        isAuth = true

        binding.resetBtn.enable(false)

        var email = ""
        if (authToken != null) {
            email = currentUserProfile.email
            binding.emailEt.setText(email)
            binding.emailEt.enable(false)
            binding.resetBtn.enable(true)
            binding.loginHere.visible(false)
        }

        binding.emailEt.addTextChangedListener {
            email = it.toString()
            binding.resetBtn.enable(it.toString().isEmailValid())
        }

        binding.loginHere.setOnClickListener {
            finishActivity()
        }

        binding.signupHereTv.visible(authToken == null)

        otpInputLive.observe(this) {
            binding.alphaBg.visible(false)
            binding.resetBtn.enable(true)
            startActivity(Intent(this,NewPasswordActivity::class.java).apply {
                putExtra("hash",hash)
                putExtra("plain",it)
                putExtra("email",email)
            })
        }

        otpLive.observe(this) {
            hash = it
            binding.alphaBg.visible(true)
            val sheet = BaseBottomSheetFragment(this)
            sheet.show(supportFragmentManager,sheet.tag)
        }

        binding.resetBtn.setOnClickListener {
            binding.resetBtn.enable(false)
            CoroutineScope(Dispatchers.IO).launch {
                when(val sendReset = viewModel.forgotPassword(email)) {
                    is Resource.Success -> {
                        if (!sendReset.value.successful)
                            binding.root.errorSnackBar("Email does not exist")
                        else
                            otpHashDataMutable.postValue(sendReset.value.data.toString())
                    }
                    else -> {}
                }
            }
        }

        binding.returnBtn.setOnClickListener {
            finishActivity()
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityForgotPasswordBinding.inflate(inflater)

    override fun getViewModel() = AuthViewModel::class.java

    override fun getActivityRepository() =
        AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun enableBtn() {
        binding.resetBtn.enable(true)
    }
}