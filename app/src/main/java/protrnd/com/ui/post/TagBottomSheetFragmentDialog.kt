package protrnd.com.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.data.models.Profile
import protrnd.com.databinding.TagUsersLayoutBinding

class TagBottomSheetFragmentDialog(
    var taggedProfiles: ArrayList<Profile> = arrayListOf(),
    val activity: NewPostActivity
) : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val request = TagUsersLayoutBinding.inflate(inflater, container, false)
        return request.root
    }

    override fun onDetach() {
        super.onDetach()
        activity.taggedProfiles = taggedProfiles
        activity.removeAlphaBackground()
    }
}