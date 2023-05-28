package protrnd.com.ui.wallet.send

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.data.models.Profile
import protrnd.com.ui.chat.ChatContentActivity
import protrnd.com.ui.home.HomeFragment
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.profile.ProfileFragment
import protrnd.com.ui.wallet.WalletFragment

class SendMoneyBottomSheetFragment(
    val fragment: Fragment = Fragment(),
    val profile: Profile = Profile(),
    val activity: Activity? = null,
    val convoid: String = ""
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val a = it as BottomSheetDialog
            a.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.send_money_layout, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        when (fragment) {
            is WalletFragment -> fragment.removeAlphaVisibility()
            is ProfileFragment -> fragment.removeAlphaVisibility()
            is HomeFragment -> fragment.removeAlphaVisibility()
        }

        when (activity) {
            is ChatContentActivity -> activity.removeAlphaVisibility()
            is ProfileActivity -> activity.removeAlphaVisibility()
        }
    }
}