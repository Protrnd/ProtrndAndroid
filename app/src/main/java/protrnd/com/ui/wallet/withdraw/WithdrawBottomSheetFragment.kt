package protrnd.com.ui.wallet.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.ui.wallet.WalletFragment

class WithdrawBottomSheetFragment(val fragment: Fragment): BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.withdraw_base_layout,container,false)
    }

    override fun onDetach() {
        super.onDetach()
        if (fragment is WalletFragment)
            fragment.removeAlphaVisibility()
    }
}