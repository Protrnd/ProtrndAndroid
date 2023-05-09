package protrnd.com.ui.support

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
import protrnd.com.data.models.Post
import protrnd.com.ui.HashTagResultsActivity
import protrnd.com.ui.home.HomeFragment
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.profile.ProfileFragment

class SupportBottomSheet(
    val fragment: Fragment? = null,
    val activity: Activity? = null,
    val post: Post
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val a = it as BottomSheetDialog
            a.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
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
        return inflater.inflate(R.layout.support_request_layout, container, false)
    }

    fun getPostValue() = post

    override fun onDetach() {
        super.onDetach()
        if (fragment is HomeFragment)
            fragment.removeAlphaVisibility()
        if (activity is PostActivity)
            activity.removeAlphaVisibility()
        if (activity is HashTagResultsActivity)
            activity.removeAlphaVisibility()
        if (activity is ProfileActivity)
            activity.removeAlphaVisibility()
        if (fragment is ProfileFragment)
            fragment.removeAlphaVisibility()
    }
}