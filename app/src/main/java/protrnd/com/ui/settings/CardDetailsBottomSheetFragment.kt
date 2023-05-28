package protrnd.com.ui.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.databinding.ChangeCardDetailsBaseLayoutBinding

class CardDetailsBottomSheetFragment: BottomSheetDialogFragment() {
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
        val v = ChangeCardDetailsBaseLayoutBinding.inflate(inflater,container,false)
        return v.root
    }
}