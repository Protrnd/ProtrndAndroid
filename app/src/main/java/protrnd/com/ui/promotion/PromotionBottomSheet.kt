package protrnd.com.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.databinding.PromoteRequestLayoutBinding
import protrnd.com.ui.home.HomeFragment

class PromotionBottomSheet(val fragment: Fragment, private val postId: String) :
    BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val request = PromoteRequestLayoutBinding.inflate(inflater, container, false)
        return request.root
    }

    fun getPostId() = postId

    override fun onDetach() {
        super.onDetach()
        if (fragment is HomeFragment)
            fragment.removeAlphaVisibility()
    }
}