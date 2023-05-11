package protrnd.com.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import protrnd.com.R
import protrnd.com.data.repository.EmptyRepository
import protrnd.com.databinding.ActivitySettingsBinding
import protrnd.com.ui.auth.ForgotPasswordActivity
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity
import protrnd.com.ui.logout
import protrnd.com.ui.startAnimation
import protrnd.com.ui.viewmodels.EmptyViewModel

class SettingsActivity : BaseActivity<ActivitySettingsBinding, EmptyViewModel, EmptyRepository>() {

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_ic)
        binding.toolbar.contentInsetStartWithNavigation = 0
        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
            startAnimation()
        }
        binding.logout.setOnClickListener {
            logout()
        }
        binding.changePassBtn.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java).apply {
                startAnimation()
            })
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) = ActivitySettingsBinding.inflate(inflater)

    override fun getViewModel() = EmptyViewModel::class.java

    override fun getActivityRepository() = EmptyRepository()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishActivity()
        }
        return true
    }
}