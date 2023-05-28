package protrnd.com.ui.post

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View.OVER_SCROLL_NEVER
import android.view.Window
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.models.Location
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.backgroundtask.UploadJobService
import protrnd.com.data.repository.PostRepository
import protrnd.com.databinding.ActivityNewPostBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.databinding.UploadProcessingBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.PostImagesAdapter
import protrnd.com.ui.adapter.listener.ImageClickListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.viewmodels.PostViewModel
import java.io.File
import java.util.*

class NewPostActivity : BaseActivity<ActivityNewPostBinding, PostViewModel, PostRepository>() {
    private var postUriList: ArrayList<Uri> = arrayListOf()
    lateinit var adapter: PostImagesAdapter
    private var currentPosition = 0
    private var selectedUri = Uri.EMPTY
    var taggedProfiles = arrayListOf<Profile>()
    override var authToken: String? = ""
    private var imageList: ArrayList<String> = ArrayList()
    private var videoList: ArrayList<String> = ArrayList()

    private val getVideoCutResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                postUriList[currentPosition] = Uri.parse(TrimVideo.getTrimmedVideoPath(result.data))
            } else {
                binding.root.errorSnackBar("No video file selected")
            }
        }

    private fun getContent() {
        val outputUri = File(filesDir, "${Date().time}.jpg").toUri()
        val listUri = listOf(postUriList[currentPosition], outputUri)
        selectedUri = postUriList[currentPosition]
        if (!this.isVideoFile(selectedUri)) {
            cropImage.launch(listUri)
        } else {
            TrimVideo.activity("$selectedUri")
                .setTrimType(TrimType.MIN_MAX_DURATION)
                .setMinToMax(5, 300)
                .start(this, getVideoCutResult)
        }
    }

    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val options = UCrop.Options()
            options.setFreeStyleCropEnabled(true)
            options.setShowCropGrid(true)
            val uCrop = UCrop.of(inputUri, outputUri)
                .withOptions(options)
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
                binding.uploadsVp.background = ColorDrawable(Color.WHITE)
                binding.postBtn.enable(true)
                postUriList.addAll(uris as ArrayList<Uri>)
                binding.uploadsVp.visible(true)
                binding.uploadsVp.clipChildren = false
                binding.uploadsVp.clipToPadding = false
                binding.uploadsVp.getChildAt(0).overScrollMode = OVER_SCROLL_NEVER
                adapter = PostImagesAdapter(
                    uri = postUriList,
                    activity = this,
                    currentProfile = currentUserProfile
                )
                binding.uploadsVp.adapter = adapter
                adapter.viewClick(object : ImageClickListener {
                    override fun imageClickListener(imageUri: Uri, position: Int) {
                        postUriList = adapter.uri as ArrayList<Uri>
                        currentPosition = binding.uploadsVp.currentItem
                        getContent()
                    }
                })
                val transformer = CompositePageTransformer()
                transformer.addTransformer(MarginPageTransformer(10))
                binding.uploadsVp.setPageTransformer(transformer)
                TabLayoutMediator(binding.tabLayout, binding.uploadsVp) { _, _ ->
                }.attach()
            } else {
                binding.uploadsVp.background = ColorDrawable(getColor(R.color.lightest_grey))
                Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        checkStoragePermissions()

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

        binding.profileName.text = currentUserProfile.fullname
        binding.selectedLocation.text = currentUserProfile.location

        binding.postBtn.enable(false)

        binding.addImageBtn.setOnClickListener {
            launchGalleryPicker()
        }

        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(this)
                .load(currentUserProfile.profileimg)
                .circleCrop()
                .into(binding.profileImg)

        binding.tagPeopleBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheetDialog = TagBottomSheetFragmentDialog(taggedProfiles, this)
            bottomSheetDialog.show(supportFragmentManager, bottomSheetDialog.tag)
        }

        val locationDialog = Dialog(this, R.style.TransparentDialog)
        val locationPickerBinding = LocationPickerBinding.inflate(layoutInflater)
        locationDialog.setCanceledOnTouchOutside(true)
        locationDialog.setCancelable(true)
        locationDialog.setContentView(locationPickerBinding.root)
        val window: Window = locationDialog.window!!
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val location = currentUserProfile.location?.split(",")
        var state = location?.get(0)
        var city = location?.get(1)
        var locationUpdate: String

        binding.addLocationBtn.setOnClickListener {
            locationDialog.show()
            locationPickerBinding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
                city = ""
                locationPickerBinding.cityPicker.clearSelectedItem()
                state = newItem
                locationPickerBinding.selectState(state!!)
                locationPickerBinding.saveBtn.enable(false)
            }

            locationPickerBinding.cityPicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
                city = newItem
                locationPickerBinding.saveBtn.enable(true)
            }

            locationPickerBinding.statePicker.selectItemByIndex(0)
            locationPickerBinding.cityPicker.selectItemByIndex(0)

            locationPickerBinding.saveBtn.setOnClickListener {
                locationUpdate = "$state,$city"
                binding.selectedLocation.text = locationUpdate
                locationDialog.dismiss()
            }
        }

        val dialog = Dialog(this, R.style.TransparentDialog)
        dialog.setCanceledOnTouchOutside(false)
        val dialogProcessing = UploadProcessingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogProcessing.root)

        binding.postBtn.setOnClickListener {
            if (isNetworkAvailable()) {
                binding.alphaBg.visible(true)
                binding.postBtn.enable(false)
                binding.captionEt.clearFocus()
                dialog.show()

                val tags = arrayListOf<String>()
                for (profile in taggedProfiles) {
                    tags.add(profile.id)
                }

                upload(
                    tags,
                    Location(city = city!!, state = state!!),
                    postUriList,
                    currentUserProfile,
                    binding.captionEt.text.toString().trim()
                )
            } else {
                binding.root.errorSnackBar("Please check your network connection!")
            }
        }
    }

    private fun upload(
        tags: List<String>,
        location: Location,
        postUriList: List<Uri>,
        currentUserProfile: Profile,
        caption: String
    ) {
        val uris = arrayListOf<String>()
        postUriList.forEach {
            uris.add(it.toString())
        }

        val serviceUpload = Intent(this, UploadJobService::class.java).apply {
            putExtra("auth",authToken)
            putExtra("caption",caption)
            putStringArrayListExtra("tags", ArrayList(tags))
            putStringArrayListExtra("uris",uris)
            putExtra("name",currentUserProfile.username)
            putExtra("city",location.city)
            putExtra("state",location.state)
        }

        startForegroundService(serviceUpload)

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            finishActivity()
        }
    }

    private fun launchGalleryPicker() {
        getImagePickerResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityNewPostBinding.inflate(inflater)

    override fun getViewModel() = PostViewModel::class.java

    override fun getActivityRepository(): PostRepository {
        authToken = runBlocking { profilePreferences.authToken.first() }
        val postApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java, authToken)
        val profileApi = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, authToken)
        return PostRepository(profileApi, postApi)
    }

    fun removeAlphaBackground() {
        binding.alphaBg.visible(false)
    }

    fun fetchVideos(): ArrayList<String> {
        val columns = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID
        )
        val imagecursor: Cursor = managedQuery(
            MediaStore.Video.Media.INTERNAL_CONTENT_URI, columns, null,
            null, ""
        )
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex =
                imagecursor.getColumnIndex(MediaStore.Video.Media.DATA)
            videoList.add(imagecursor.getString(dataColumnIndex))
        }
        return videoList
    }

    fun fetchImages(): ArrayList<String> {
        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID
        )
        val imagecursor: Cursor = contentResolver.query(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null,
            null, null
        )!!
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex =
                imagecursor.getColumnIndex(MediaStore.Images.Media.DATA)
            imageList.add(imagecursor.getString(dataColumnIndex))
        }
        imagecursor.close()
        return imageList
    }
}