package protrnd.com.ui.post

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaPlayer.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.R
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.FullImageVideoLayoutBinding
import protrnd.com.ui.adapter.FullImagePostAdapter
import protrnd.com.ui.enable
import protrnd.com.ui.hideSystemUI
import protrnd.com.ui.showSystemUI
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.visible

class FullImageVideoDialogFragment(
    val viewModel: HomeViewModel?,
    val post: Post?,
    private val imageUrls: List<String>,
    val position: Int,
    val currentProfile: Profile
) : BottomSheetDialogFragment() {
    var pausePosition = 0
    lateinit var binding: FullImageVideoLayoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            requireView().hideSystemUI(requireActivity().window)
            val a = it as BottomSheetDialog
            a.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            a.behavior.state = STATE_EXPANDED
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireView().showSystemUI(requireActivity().window)
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
        binding = FullImageVideoLayoutBinding.inflate(inflater, container, false)
        binding.root.minHeight = Resources.getSystem().displayMetrics.heightPixels

        binding.closeVideo.setOnClickListener {
            requireView().showSystemUI(requireActivity().window)
            dismiss()
        }

        if (currentProfile.id == post!!.profileid)
            binding.support.visible(false)

        viewModel?.viewModelScope?.launch {
            val isLiked = viewModel.postIsLiked(post.id)
            binding.likeToggle.isChecked = isLiked.data!!.data
        }

        binding.likeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                binding.likeToggle.isClickable = false
        }

        binding.likeToggle.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel!!.likePost(post.id)
            }
        }

        if (imageUrls[position].contains(".mp4")) {
            binding.videoView.visible(true)
            binding.seekbar.visible(true)
            binding.toggleButton.visible(true)
            binding.progressBar.visible(true)
            binding.seekbar.enable(false)
            binding.imageFullView.visible(false)
            binding.toggleButton.enable(false)
            binding.videoView.setVideoPath(imageUrls[position])
            binding.videoView.start()
            binding.videoView.setOnInfoListener { _, what, _ ->
                when (what) {
                    MEDIA_INFO_VIDEO_RENDERING_START -> {
                        binding.seekbar.enable(true)
                        binding.progressBar.visible(false)
                        binding.toggleButton.enable(true)
                        return@setOnInfoListener true
                    }
                    MEDIA_INFO_BUFFERING_START -> {
                        binding.progressBar.visible(true)
                        binding.seekbar.enable(false)
                        binding.toggleButton.enable(false)
                        return@setOnInfoListener true
                    }
                    MEDIA_INFO_BUFFERING_END -> {
                        binding.progressBar.visible(false)
                        binding.seekbar.enable(true)
                        binding.toggleButton.enable(true)
                        return@setOnInfoListener true
                    }
                }
                false
            }

            binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) {
                    if (binding.videoView.isPlaying) {
                        binding.videoView.pause()
                        pausePosition = binding.videoView.currentPosition
                    }
                } else {
                    if (!binding.videoView.isPlaying) {
                        binding.videoView.seekTo(pausePosition)
                        binding.videoView.start()
                    }
                }
            }

            binding.videoView.isDrawingCacheEnabled = true

            CoroutineScope(Dispatchers.IO).launch {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(imageUrls[position], HashMap<String, String>())
                val time = retriever.extractMetadata(METADATA_KEY_DURATION)
                val timeInMillis = time!!.toLong()
                retriever.release()
                val duration = convertMillieToHHSS(timeInMillis)
                withContext(Dispatchers.Main) {
                    binding.timeStamp.text = duration
                }
            }

            val handler = Handler(Looper.getMainLooper())

            val updateTime = object : Runnable {
                override fun run() {
                    binding.seekbar.progress = binding.videoView.currentPosition
                    binding.seekbar.max = binding.videoView.duration
                    handler.postDelayed(this, 100)
                }
            }

            handler.postDelayed(updateTime, 100)

            binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    handler.removeCallbacks(updateTime)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    handler.removeCallbacks(updateTime)
                    binding.videoView.seekTo(binding.seekbar.progress)
                    handler.postDelayed(updateTime, 100)
                }
            })

            binding.videoView.setOnCompletionListener {
                binding.videoView.stopPlayback()
                binding.videoView.clearFocus()
                dismiss()
            }
            binding.closeVideo.setOnClickListener {
                binding.videoView.stopPlayback()
                binding.videoView.clearFocus()
                dismiss()
            }
        } else {
            binding.videoView.visible(false)
            binding.seekbar.visible(false)
            binding.toggleButton.visible(false)
            binding.progressBar.visible(false)
            binding.videoView.stopPlayback()
            binding.videoView.visible(false)
            binding.imageFullView.visible(true)
            val imagesPager = binding.imageFullView
            imagesPager.clipChildren = false
            imagesPager.clipToPadding = false
            imagesPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

            val adapter = FullImagePostAdapter(
                this.childFragmentManager,
                images = imageUrls,
                currentProfile,
                post
            )
            imagesPager.adapter = adapter
        }

        binding.support.setOnClickListener {
            binding.toggleButton.isChecked = false
            val supportBottomSheetDialogFragment = SupportBottomSheet(post = post)
            supportBottomSheetDialogFragment.show(
                childFragmentManager,
                supportBottomSheetDialogFragment.tag
            )
        }
        return binding.root
    }

    private fun convertMillieToHHSS(timeInMillis: Long): String {
        val seconds = timeInMillis / 1000
        val second = seconds % 60
        val minute = (seconds / 60) % 60
        val hour = (seconds / (60 * 60)) % 24
        return if (hour > 0)
            String.format("%02d:%02d:%02d", hour, minute, second)
        else
            String.format("%02d:%02d", minute, second)
    }

    override fun onPause() {
        binding.toggleButton.isChecked = false
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoView.seekTo(pausePosition)
    }
}