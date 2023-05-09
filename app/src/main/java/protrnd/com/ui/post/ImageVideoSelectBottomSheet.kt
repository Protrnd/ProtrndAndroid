package protrnd.com.ui.post

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.databinding.ImageVideoSelectionLayoutBinding
import protrnd.com.ui.addThumbnailGrid3

class ImageVideoSelectBottomSheet(
    val activity: NewPostActivity,
    val images: ArrayList<String>,
    val videos: ArrayList<String>
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val a = it as BottomSheetDialog
            a.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setCanceledOnTouchOutside(true)
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
    ): View {
        val binding = ImageVideoSelectionLayoutBinding.inflate(inflater, container, false)
        binding.imageVideoRv.addThumbnailGrid3(requireContext(), images)

        binding.imageVideoSpinner.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            when (newIndex) {
                0 -> {
                    binding.imageVideoRv.addThumbnailGrid3(requireContext(), images)
                }
                1 -> {
                    binding.imageVideoRv.addThumbnailGrid3(requireContext(), videos)
                }
            }
        }
        binding.imageVideoSpinner.selectItemByIndex(0)

        return binding.root
    }


    override fun onDetach() {
        super.onDetach()
    }
}