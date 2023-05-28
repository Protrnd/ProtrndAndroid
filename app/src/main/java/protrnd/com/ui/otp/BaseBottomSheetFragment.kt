package protrnd.com.ui.otp

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.databinding.FragmentVerifyResetOtpBinding
import protrnd.com.ui.auth.ForgotPasswordActivity
import protrnd.com.ui.errorSnackBar
import protrnd.com.ui.requestForFocus
import protrnd.com.ui.settings.SettingsActivity

class BaseBottomSheetFragment(val activity: Activity): BottomSheetDialogFragment() {
    private var otp1 = ""
    private var otp2 = ""
    private var otp3 = ""
    private var otp4 = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val a = it as BottomSheetDialog
            val parentLayout = a.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {  sheet ->
                val behaviour = BottomSheetBehavior.from(sheet)
                setupFullHeight(sheet)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    private fun setupFullHeight(bottomSheet: View) {
        val params = bottomSheet.layoutParams
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = params
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val request = FragmentVerifyResetOtpBinding.inflate(inflater, container, false)
        request.input1.requestForFocus(request.input2)
        request.input2.requestForFocus(request.input3, request.input1)
        request.input3.requestForFocus(request.input4, request.input2)
        request.input4.requestForFocus(prev = request.input3)
        request.continueBtn.setOnClickListener {
            otp1 = request.input1.text.toString()
            otp2 = request.input2.text.toString()
            otp3 = request.input3.text.toString()
            otp4 = request.input4.text.toString()
            if (otp1.isNotEmpty() && otp2.isNotEmpty() && otp3.isNotEmpty() && otp4.isNotEmpty()) {
                val inputOtp = "$otp1$otp2$otp3$otp4"
                if (activity is ForgotPasswordActivity) {
                    activity.otpInputMutable.postValue(inputOtp)
                    dismiss()
                }
                if (activity is SettingsActivity) {
                    activity.sentOTPLive.observe(viewLifecycleOwner) {
                        if (inputOtp == it) {
                            activity.inputOTPMutable.postValue(inputOtp)
                            dismiss()
                        } else {
                            request.root.errorSnackBar("Invalid OTP, please check your email for the latest OTP sent to you")
                        }
                    }
                }
            }
        }
        return request.root
    }


    override fun onDetach() {
        super.onDetach()
        if (activity is ForgotPasswordActivity)
            activity.removeAlphaVisibility()
    }
}