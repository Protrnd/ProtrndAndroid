package protrnd.com.ui.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.satoshun.coroutine.autodispose.lifecycle.autoDisposeScope
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfileBinding
import protrnd.com.databinding.LoadingLayoutBinding
import protrnd.com.databinding.SelectImageDialogBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ImageThumbnailPostAdapter
import protrnd.com.ui.adapter.listener.ImagePostItemClickListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.post.PostActivity
import java.io.File
import java.util.*

class ProfileFragment : BaseFragment<HomeViewModel, FragmentProfileBinding, HomeRepository>() {

    private var profile: Profile? = null
    private var profileMap: HashMap<String, Profile> = HashMap()
    private lateinit var loadingDialog : Dialog

    private val getProfileImageContent =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val outputUri = File(requireActivity().filesDir, "${Date().time}.jpg").toUri()
                val listUri = listOf(uri, outputUri)
                cropProfileImage.launch(listUri)
            }
        }

    private val cropProfileImage = registerForActivityResult(cropImagePicker(1f, 1f, 1080)) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            Glide.with(requireContext()).load(uri).into(binding.profileImage)
            loadingDialog.show()

            autoDisposeScope.launch {
                val uploadResult = viewModel.uploadImage(
                    uri,
                    profile?.username.toString(),
                    requireActivity().getFileTypes(listOf(uri))[0]
                )
                withContext(Dispatchers.Main) {
                    uploadResult.observe(viewLifecycleOwner) { url ->
                        if (url.isEmpty()) {
                            binding.root.snackbar("Error")
                        } else {
                            viewModel.updateProfile(
                                ProfileDTO(
                                    profileImage = url,
                                    backgroundImageUrl = profile!!.bgimg,
                                    phone = profile!!.phone!!,
                                    accountType = profile?.acctype!!,
                                    location = profile!!.location!!,
                                    email = profile!!.email,
                                    fullName = profile!!.fullname,
                                    userName = profile!!.username
                                )
                            )
                        }
                    }
                    loadingDialog.dismiss()
                }
            }

            Glide.with(requireContext()).load(uri).circleCrop().into(binding.profileImage)
        }
    }

    private val cropBannerImage = registerForActivityResult(cropImagePicker(16f, 9f, 1920)) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            loadingDialog.show()
            Glide.with(requireContext()).load(uri).into(binding.bgImage)
            autoDisposeScope.launch {
                val result = viewModel.uploadImage(
                    uri,
                    profile?.username.toString(),
                    requireActivity().getFileTypes(listOf(uri))[0]
                )
                withContext(Dispatchers.Main) {
                    result.observe(viewLifecycleOwner) { url ->
                        if (url.isEmpty()) {
                            binding.root.snackbar("Error")
                        } else {
                            viewModel.updateProfile(
                                ProfileDTO(
                                    profileImage = profile!!.profileimg,
                                    backgroundImageUrl = url,
                                    phone = profile!!.phone!!,
                                    accountType = profile?.acctype!!,
                                    location = profile!!.location!!,
                                    email = profile!!.email,
                                    fullName = profile!!.fullname,
                                    userName = profile!!.username
                                )
                            )
                        }
                    }
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private val getBannerImageContent =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val outputUri = File(requireActivity().filesDir, "${Date().time}.jpg").toUri()
                val listUri = listOf(uri, outputUri)
                cropBannerImage.launch(listUri)
            }
        }

    private fun cropImagePicker(ratioX: Float, ratioY: Float, width: Int) =
        object : ActivityResultContract<List<Uri>, Uri>() {
            override fun createIntent(context: Context, input: List<Uri>): Intent {
                val inputUri = input[0]
                val outputUri = input[1]

                val uCrop = UCrop.of(inputUri, outputUri).withAspectRatio(ratioX, ratioY)
                    .withMaxResultSize(width, 1080)

                return uCrop.getIntent(context)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri {
                return try {
                    UCrop.getOutput(intent!!)!!
                } catch(e: Exception) {
                    Uri.EMPTY
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = Dialog(requireContext())
        loadingDialog.setCanceledOnTouchOutside(false)
        val loadingLayoutBinding = LoadingLayoutBinding.inflate(layoutInflater)
        loadingDialog.setContentView(loadingLayoutBinding.root)
        val loadingWindow: Window = loadingDialog.window!!
        loadingWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingWindow.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        requireActivity().checkStoragePermissions()

        loadPage()
        viewModel.profile.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    profileMap["profile"] = it.value.data
                    profile = profileMap["profile"]
                    binding.profileFullName.text = profile?.fullname
                    val username = "@${profile?.username.toString()}"
                    binding.profileUsername.text = username
                    if (profile?.profileimg!!.isNotEmpty()) Glide.with(this)
                        .load(profile?.profileimg).circleCrop().into(binding.profileImage)
                    if (profile?.bgimg!!.isNotEmpty()) Glide.with(this).load(profile?.bgimg)
                        .into(binding.bgImage)

                    lifecycleScope.launch {
                        binding.followersCount.showFollowersCount(viewModel, profile!!)
                        binding.followingCount.showFollowingCount(viewModel, profile!!)
                        binding.postsRv.showUserPostsInGrid(requireContext(),viewModel,profile!!)
                        val thumbnailAdapter = binding.postsRv.adapter as ImageThumbnailPostAdapter
                        thumbnailAdapter.imageClickListener(object : ImagePostItemClickListener {
                            override fun postItemClickListener(post: Post) {
                                startActivity(Intent(requireContext(), PostActivity::class.java).apply {
                                    this.putExtra("post_id",post.identifier)
                                })
                            }
                        })
                        binding.profileShimmer.visible(false)
                        binding.profileView.visible(true)
                    }
                }
                is Resource.Loading -> {
                    binding.profileShimmer.visible(true)
                    binding.profileView.visible(false)
                }
                is Resource.Failure -> {
                    binding.root.snackbar("Error loading profile") { loadPage() }
                }
            }
        }

        val dialog = Dialog(requireContext())
        val selectBinding = SelectImageDialogBinding.inflate(layoutInflater)
        dialog.setContentView(selectBinding.root)
        val window: Window = dialog.window!!
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.bgImage.setOnClickListener {
            val request = "Want to upload a new background image?"
            selectBinding.actionRequest.text = request
            selectBinding.acceptBtn.setOnClickListener {
                getBannerImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.profileImage.setOnClickListener {
            val request = "Want to upload a new profile photo?"
            selectBinding.actionRequest.text = request
            selectBinding.acceptBtn.setOnClickListener {
                getProfileImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    private fun loadPage() {
        viewModel.getCurrentProfile()
    }
}