package protrnd.com.ui.post

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.OVER_SCROLL_NEVER
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.UploadService
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PostRepository
import protrnd.com.databinding.ActivityNewPostBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.PostImagesAdapter
import protrnd.com.ui.adapter.ProfileTagAdapter
import protrnd.com.ui.adapter.listener.ImageClickListener
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.base.BaseActivity
import java.io.File
import java.util.*

class NewPostActivity : BaseActivity<ActivityNewPostBinding, PostViewModel, PostRepository>() {

    private var postUriList: ArrayList<Uri> = arrayListOf()
    lateinit var adapter: PostImagesAdapter
    private var currentPosition = 0
    private var selectedUri = Uri.EMPTY
    private lateinit var tagsAdapter: ProfileTagAdapter
    var authToken = ""

    private fun getContent() {
        val outputUri = File(filesDir, "${Date().time}.jpg").toUri()
        val listUri = listOf(postUriList[currentPosition], outputUri)
        selectedUri = postUriList[currentPosition]
        if (!this.isVideoFile(selectedUri)) {
            cropImage.launch(listUri)
        } else {
            binding.root.snackbar("Cannot scale video file, please select another file")
        }
    }

    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(4f, 3f)
                .withMaxResultSize(1920, 1440)
            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            if (intent != null) {
                return try {
                    UCrop.getOutput(intent)!!
                } catch (e: Exception) {
                    selectedUri
                }
            }
            return selectedUri
        }
    }

    private val cropImage = registerForActivityResult(uCropContract) { uri ->
        if (uri != null) {
            postUriList[currentPosition] = uri
            adapter.notifyItemChanged(currentPosition)
        }
    }

    private val getImagePickerResult =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                binding.postBtn.enable(true)
                postUriList = uris as ArrayList<Uri>
                binding.uploadsVp.visible(true)
                binding.scaleImage.visible(true)
                binding.uploadsVp.clipChildren = false
                binding.uploadsVp.clipToPadding = false
                binding.uploadsVp.getChildAt(0).overScrollMode = OVER_SCROLL_NEVER
                adapter = PostImagesAdapter(uri = postUriList, activity = this)
                binding.uploadsVp.adapter = adapter
                adapter.viewClick(object : ImageClickListener {
                    override fun imageClickListener(imageUri: Uri, position: Int) {
                        postUriList = adapter.uri as ArrayList<Uri>
                        launchGalleryPicker()
                    }
                })
                binding.selectToAddTv.visible(false)
                val transformer = CompositePageTransformer()
                transformer.addTransformer(MarginPageTransformer(10))
                binding.uploadsVp.setPageTransformer(transformer)
                TabLayoutMediator(binding.tabLayout, binding.uploadsVp) { _, _ ->
                }.attach()
            } else {
                Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        this.checkStoragePermissions()

        binding.cancelButton.setOnClickListener {
            if (postUriList.isNotEmpty())
                showConfirmationDialog(
                    layoutInflater,
                    "Continue Editing",
                    "Dismiss",
                    title = "Are you sure you want to dismiss your progress?",
                    action = { finishActivity() }
                )
            else
                finishActivity()
        }

        binding.postBtn.enable(false)

        binding.uploadsCard.setOnClickListener {
            launchGalleryPicker()
        }

        if (postUriList.isEmpty())
            binding.scaleImage.visible(false)

        binding.scaleImage.setOnClickListener {
            currentPosition = binding.uploadsVp.currentItem
            getContent()
        }

        binding.postBtn.setOnClickListener {
            binding.postBtn.enable(false)
            binding.captionEt.clearFocus()
            val serviceIntent = Intent(this, UploadService::class.java)
            serviceIntent.putExtra("auth_token", authToken)
            serviceIntent.putExtra("posts", postUriList)
            serviceIntent.putExtra("profile", currentUserProfile)
            serviceIntent.putExtra("caption", binding.captionEt.text.toString().trim())
            startService(serviceIntent)
            Toast.makeText(
                this,
                "We will notify you when your upload is completed",
                Toast.LENGTH_SHORT
            ).show()
            finishActivity()
        }

        binding.captionEt.addTextChangedListener { s ->
            val text = s.toString()
            if (text.lastIndexOf("@") > text.lastIndexOf(" ")) {
                val tag = text.substring(text.lastIndexOf("@"), text.length).removePrefix("@")
                if (text[text.length - 1] != '@') {
                    getProfilesByUsername(tag)
                    viewModel.profiles.observe(this) {
                        when (it) {
                            is Resource.Success -> {
                                binding.tagsRecyclerview.visible(true)
                                binding.shimmerProfiles.visible(false)
                                tagsAdapter = ProfileTagAdapter(it.value.data)
                                binding.tagsRecyclerview.layoutManager =
                                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                                binding.tagsRecyclerview.adapter = tagsAdapter
                                tagsAdapter.clickPosition(object : ProfileClickListener {
                                    override fun profileClick(profile: Profile) {
                                        val start = text.length - tag.length - 1
                                        binding.captionEt.text.replace(
                                            start,
                                            text.length,
                                            "@${profile.username} "
                                        )
                                        binding.tagsRecyclerview.visible(false)
                                    }
                                })
                                tagsAdapter.notifyDataSetChanged()
                                viewModel.profiles.removeObservers(this)
                            }
                            is Resource.Loading -> {
                                if (text[text.length - 2] == '@')
                                    binding.shimmerProfiles.visible(true)
                                binding.tagsRecyclerview.visible(false)
                            }
                            is Resource.Failure -> {
                                handleAPIError(binding.root, it) { getProfilesByUsername(tag) }
                                binding.shimmerProfiles.visible(false)
                                binding.tagsRecyclerview.visible(false)
                            }
                            else -> {}
                        }
                    }
                }
            } else {
                binding.tagsRecyclerview.visible(false)
            }
        }
    }

    fun launchGalleryPicker() {
        getImagePickerResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityNewPostBinding.inflate(inflater)

    override fun getViewModel() = PostViewModel::class.java

    override fun getActivityRepository(): PostRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        authToken = token!!
        val postApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java, token)
        val profileApi = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, token)
        return PostRepository(profileApi, postApi)
    }

    private fun getProfilesByUsername(tag: String) {
        viewModel.getProfilesByUsername(tag)
    }
}