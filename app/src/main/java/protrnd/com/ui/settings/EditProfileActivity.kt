package protrnd.com.ui.settings

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.Window
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityEditProfileBinding
import protrnd.com.databinding.EditImageLayoutBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.enable
import protrnd.com.ui.finishActivity
import protrnd.com.ui.getFileTypes
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.visible
import java.io.File
import java.util.*

class EditProfileActivity :
    BaseActivity<ActivityEditProfileBinding, HomeViewModel, HomeRepository>() {

    private var profileUri: Uri = Uri.EMPTY
    private var bannerUri: Uri = Uri.EMPTY
    private var profileImageMutable: MutableLiveData<String> = MutableLiveData()
    private var profileImageLive: LiveData<String> = profileImageMutable
    private var bannerImageMutable: MutableLiveData<String> = MutableLiveData()
    private var bannerImageLive: LiveData<String> = bannerImageMutable
    private var profileImageUrl = ""
    private var bannerImageUrl = ""

    private val getProfileImageContent =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                profileUri = uri
                cropProfile(uri)
            }
        }

    private val cropProfileImage = registerForActivityResult(cropImagePicker(1f, 1f, 1080)) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            profileUri = uri
            binding.finish.enable(true)
            Glide.with(this).load(uri).circleCrop().into(binding.profileImage)
        }
    }

    private val cropBannerImage = registerForActivityResult(cropImagePicker(16f, 9f, 1920)) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            bannerUri = uri
            binding.finish.enable(true)
            Glide.with(this).load(uri).into(binding.backgroundImage)
        }
    }

    private val getBannerImageContent =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                bannerUri = uri
                cropBanner(uri)
            }
        }

    private fun cropBanner(uri: Uri) {
        val outputUri = File(filesDir, "${Date().time}.jpg").toUri()
        val listUri = listOf(uri, outputUri)
        cropBannerImage.launch(listUri)
    }

    private fun cropProfile(uri: Uri) {
        val outputUri = File(filesDir, "${Date().time}.jpg").toUri()
        val listUri = listOf(uri, outputUri)
        cropProfileImage.launch(listUri)
    }

    private fun cropImagePicker(ratioX: Float, ratioY: Float, width: Int) =
        object : ActivityResultContract<List<Uri>, Uri>() {
            override fun createIntent(context: Context, input: List<Uri>): Intent {
                val inputUri = input[0]
                val outputUri = input[1]
                val options = UCrop.Options()
                options.setFreeStyleCropEnabled(true)
                options.setShowCropGrid(true)
                val uCrop = UCrop.of(inputUri, outputUri)
                    .withOptions(options)
                    .withAspectRatio(ratioX, ratioY)
                    .withMaxResultSize(width, 1080)

                return uCrop.getIntent(context)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri {
                return try {
                    UCrop.getOutput(intent!!)!!
                } catch (e: Exception) {
                    Uri.EMPTY
                }
            }
        }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.contentInsetStartWithNavigation = 0
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_ic)

        if (currentUserProfile.bgimg.isNotEmpty())
            Glide.with(this)
                .load(currentUserProfile.bgimg)
                .into(binding.backgroundImage)

        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(this)
                .load(currentUserProfile.profileimg)
                .circleCrop()
                .into(binding.profileImage)

        binding.fullNameInput.setText(currentUserProfile.fullname)
        binding.aboutInput.setText(currentUserProfile.about)
        binding.locationInput.text = currentUserProfile.location

        binding.finish.enable(false)

        var fullname = ""
        binding.fullNameInput.addTextChangedListener {
            if (it.toString().isEmpty())
                binding.fullNameInput.error = "Please fill"
            else
                fullname = it.toString()

            binding.finish.enable(it.toString().isNotEmpty())
        }

        val editorBinding = EditImageLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(this, R.style.TransparentDialog)
        dialog.setContentView(editorBinding.root)
        val editWindow: Window = dialog.window!!
        editWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.profileImage.setOnClickListener {
            editorBinding.replaceImage.setOnClickListener {
                dialog.dismiss()
                getProfileImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            editorBinding.cropImage.setOnClickListener {
                dialog.dismiss()
                cropProfile(profileUri)
            }
            dialog.show()
        }

        binding.backgroundImage.setOnClickListener {
            editorBinding.replaceImage.setOnClickListener {
                dialog.dismiss()
                getBannerImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            editorBinding.cropImage.setOnClickListener {
                dialog.dismiss()
                cropProfile(bannerUri)
            }
            dialog.show()
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
        var locationUpdate = currentUserProfile.location ?: ""

        binding.locationInput.setOnClickListener {
            locationDialog.show()
            locationPickerBinding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
                city = ""
                locationPickerBinding.cityPicker.clearSelectedItem()
                state = newItem
                when (state!!.lowercase()) {
                    "abia" -> locationPickerBinding.cityPicker.setItems(R.array.abia)
                    "adamawa" -> locationPickerBinding.cityPicker.setItems(R.array.adamawa)
                    "akwa ibom" -> locationPickerBinding.cityPicker.setItems(R.array.akwa_ibom)
                    "anambra" -> locationPickerBinding.cityPicker.setItems(R.array.anambra)
                }
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
                binding.locationInput.text = locationUpdate
                locationDialog.dismiss()
            }
        }

        val images = mutableMapOf<String, String>()
        val mutableLiveDataList = MutableLiveData<MutableMap<String, String>>()
        val live: LiveData<MutableMap<String, String>> = mutableLiveDataList

        profileImageLive.observe(this) {
            profileImageUrl = it
            images["profile_image"] = it
            mutableLiveDataList.postValue(images)
        }

        bannerImageLive.observe(this) {
            bannerImageUrl = it
            images["banner_image"] = it
            mutableLiveDataList.postValue(images)
        }

        live.observe(this) {
            val profileUrl = it["profile_image"]
            val bannerUrl = it["banner_image"]
            if (profileUrl != null && bannerUrl != null) {
                val dto = ProfileDTO(
                    currentUserProfile.acctype,
                    email = currentUserProfile.email,
                    userName = currentUserProfile.username,
                    backgroundImageUrl = bannerUrl,
                    profileImage = profileUrl,
                    fullName = fullname,
                    location = locationUpdate,
                    about = binding.aboutInput.text.toString().trim()
                )
                viewModel.updateProfile(dto)
            }
        }

        viewModel.profile.observe(this) {
            when (it) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        profilePreferences.saveProfile(it.value.data)
                    }
                    finishActivity()
                }
                else -> {}
            }
        }

        binding.finish.setOnClickListener {
            binding.finish.visible(false)
            binding.progressBar.visible(true)
            binding.root.enable(false)

            if (profileUri != Uri.EMPTY)
                uploadImage(profileUri, true)
            else {
                if (currentUserProfile.profileimg.isNotEmpty())
                    profileImageMutable.postValue(currentUserProfile.profileimg)
                else
                    profileImageMutable.postValue("")
            }

            if (bannerUri != Uri.EMPTY)
                uploadImage(bannerUri, false)
            else {
                if (currentUserProfile.bgimg.isNotEmpty())
                    bannerImageMutable.postValue(currentUserProfile.bgimg)
                else
                    bannerImageMutable.postValue("")
            }
        }
    }

    private fun uploadImage(imageUri: Uri, isProfileImage: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val uploadResult = viewModel.uploadImage(
                imageUri,
                currentUserProfile.username,
                getFileTypes(listOf(imageUri))[0]
            )

            withContext(Dispatchers.Main) {
                uploadResult.observe(this@EditProfileActivity) {
                    if (isProfileImage)
                        profileImageMutable.postValue(it)
                    else
                        bannerImageMutable.postValue(it)
                }
            }
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityEditProfileBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        val postDatabase = protrndAPIDataSource.providePostDatabase(application)
        val profileDatabase = protrndAPIDataSource.provideProfileDatabase(application)
        return HomeRepository(api, postsApi, postDatabase, profileDatabase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishActivity()
        }
        return true
    }

}