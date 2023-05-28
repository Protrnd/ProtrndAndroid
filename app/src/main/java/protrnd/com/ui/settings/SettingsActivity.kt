package protrnd.com.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.ActivitySettingsBinding
import protrnd.com.databinding.FragmentPaymentPinBinding
import protrnd.com.ui.*
import protrnd.com.ui.auth.ForgotPasswordActivity
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.otp.BaseBottomSheetFragment
import protrnd.com.ui.viewmodels.PaymentViewModel

class SettingsActivity :
    BaseActivity<ActivitySettingsBinding, PaymentViewModel, PaymentRepository>() {

    private val sentOTPMutable = MutableLiveData<String>()
    val sentOTPLive: LiveData<String> = sentOTPMutable
    val inputOTPMutable = MutableLiveData<String>()
    private val inputOTPLive: LiveData<String> = inputOTPMutable
    private var pin1 = ""
    private var pin2 = ""
    private var pin3 = ""
    private var pin4 = ""

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
            startActivity(Intent(this, ForgotPasswordActivity::class.java).apply {
                startAnimation()
            })
        }

        inputOTPLive.observe(this) {
            if (it.isNotEmpty()) {
                setNewPaymentPin()
            }
        }

        binding.forgotPinBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                when (val reset = viewModel.setResetOTPForPin()) {
                    is Resource.Success -> {
                        sentOTPMutable.postValue("${reset.value.data}".replace(".0",""))
                    }
                    else -> {}
                }
            }
            val sheet = BaseBottomSheetFragment(this)
            sheet.show(supportFragmentManager, sheet.tag)
        }

        binding.changeCardBtn.setOnClickListener {
            val sheet = CardDetailsBottomSheetFragment()
            sheet.show(supportFragmentManager, sheet.tag)
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivitySettingsBinding.inflate(inflater)

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getActivityRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishActivity()
        }
        return true
    }

    private fun setNewPaymentPin() {
        binding.alphaBg.visible(true)
        val pinBottomSheet = BottomSheetDialog(this, R.style.BottomSheetTheme)
        val pinFrag = FragmentPaymentPinBinding.inflate(layoutInflater)
        pinBottomSheet.setContentView(pinFrag.root)
        pinBottomSheet.setOnDismissListener {
            removeAlphaVisibility()
        }
        pinFrag.input1.requestForFocus(pinFrag.input2)
        pinFrag.input2.requestForFocus(pinFrag.input3, pinFrag.input1)
        pinFrag.input3.requestForFocus(pinFrag.input4, pinFrag.input2)
        pinFrag.input4.requestForFocus(prev = pinFrag.input3)
        pinBottomSheet.show()
        pinFrag.continueBtn.setOnClickListener {
            pinFrag.continueBtn.enable(false)
            pin1 = pinFrag.input1.text.toString()
            pin2 = pinFrag.input2.text.toString()
            pin3 = pinFrag.input3.text.toString()
            pin4 = pinFrag.input4.text.toString()
            if (pin1.isNotEmpty() && pin2.isNotEmpty() && pin3.isNotEmpty() && pin4.isNotEmpty()) {
                val pin = "$pin1$pin2$pin3$pin4"
                lifecycleScope.launch {
                    when (val pinRequest = viewModel.setPaymentPin(pin)) {
                        is Resource.Success -> {
                            if (pinRequest.value.successful) {
                                val pinValue = "${pinRequest.value.data}"
                                if (pinValue == pin) {
                                    profilePreferences.savePaymentPin(pin)
                                    pinBottomSheet.dismiss()
                                    binding.alphaBg.visible(false)
                                }
                            }
                        }
                        is Resource.Failure -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "There was a network error, please try again!",
                                Toast.LENGTH_SHORT
                            ).show()
                            pinFrag.continueBtn.enable(true)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

}