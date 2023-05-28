package protrnd.com.ui.post

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.work.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.data.models.Profile
import protrnd.com.data.network.backgroundtask.DeletePostService
import protrnd.com.data.network.backgroundtask.SaveMessagesService
import protrnd.com.databinding.MoreOptionsSheetBinding
import protrnd.com.ui.*
import protrnd.com.ui.home.HomeFragment

class MoreInfoBottomSheetDialogFragment(val postId: String, val time: String, val profile: Profile, val myProfileId: String, val authToken: String): BottomSheetDialogFragment() {

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
        val parent = parentFragment
        val parentA = requireActivity()
        if (parent is HomeFragment)
            parent.showAlpha()
        if (parentA is PostActivity)
            parentA.showAlpha()
        if (parentA is HashTagResultsActivity)
            parentA.showAlpha()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MoreOptionsSheetBinding.inflate(inflater,container,false)
        val about = "About Profile: ${profile.about ?: ""}"
        val uploadDate = "Upload Date: ${getTimeWithCenterDot(time)}"
        binding.aboutProfile.text = about.setSpannableBold("About Profile:")
        binding.timeUpload.text = uploadDate.setSpannableBold("Upload Date:")
        binding.deletePostBtn.visible(profile.id == myProfileId)
        binding.deletePostBtn.setOnClickListener {
            deletePost()
            Toast.makeText(requireContext(), "Your post has been deleted. It will take a while to reflect", Toast.LENGTH_LONG).show()
            dismiss()
            if (requireActivity() is PostActivity)
                requireActivity().finishActivity()
        }
        return binding.root
    }

    private fun deletePost() {
        val data = Data.Builder()
            .putString("token", authToken)
            .putString("id",postId)
            .build()

        val worker = OneTimeWorkRequest.Builder(DeletePostService::class.java)
            .setInputData(data)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        WorkManager.getInstance(requireContext()).enqueue(worker)
    }

    override fun onDestroy() {
        super.onDestroy()
        val parent = parentFragment
        val parentA = requireActivity()
        if (parent is HomeFragment)
            parent.removeAlphaVisibility()
        if (parentA is PostActivity)
            parentA.removeAlphaVisibility()
        if (parentA is HashTagResultsActivity)
            parentA.removeAlphaVisibility()
    }
}