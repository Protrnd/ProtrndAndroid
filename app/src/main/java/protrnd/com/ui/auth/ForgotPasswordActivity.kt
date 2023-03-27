package protrnd.com.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import protrnd.com.data.network.api.AuthApi
import protrnd.com.data.repository.AuthRepository
import protrnd.com.databinding.ActivityForgotPasswordBinding
import protrnd.com.ui.base.BaseActivity

class ForgotPasswordActivity : BaseActivity<ActivityForgotPasswordBinding, AuthViewModel, AuthRepository>() {

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

    }

    override fun getActivityBinding(inflater: LayoutInflater)= ActivityForgotPasswordBinding.inflate(inflater)

    override fun getViewModel() = AuthViewModel::class.java

    override fun getActivityRepository() = AuthRepository(protrndAPIDataSource.buildAPI(AuthApi::class.java), profilePreferences)
}