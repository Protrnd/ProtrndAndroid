package protrnd.com.ui.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfileBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ProfileTabsAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment

class ProfileFragment : BaseFragment<HomeViewModel, FragmentProfileBinding, HomeRepository>() {

    private lateinit var loadingDialog: Dialog
    private lateinit var thisActivity: HomeActivity
    private val postsSizeMutable = MutableLiveData<Int>()
    private val sizeLive: LiveData<Int> = postsSizeMutable
    private val followersCachedMutable = MutableLiveData<String>()
    private val followersLive: LiveData<String> = followersCachedMutable
    private val followingsCachedMutable = MutableLiveData<String>()
    private val followingsLive: LiveData<String> = followingsCachedMutable
    private val profilePostsMutableCache = MutableLiveData<MutableList<Post>>()
    val profilePostsLive: LiveData<MutableList<Post>> = profilePostsMutableCache

//    private val getProfileImageContent =
//        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            if (uri != null) {
//                val outputUri = File(requireActivity().filesDir, "${Date().time}.jpg").toUri()
//                val listUri = listOf(uri, outputUri)
//                cropProfileImage.launch(listUri)
//            }
//        }

//    private val cropProfileImage = registerForActivityResult(cropImagePicker(1f, 1f, 1080)) { uri ->
//        if (uri != null && uri != Uri.EMPTY) {
//            Glide.with(requireContext()).load(uri).into(binding.profileImage)
//            loadingDialog.show()
//
//            autoDisposeScope.launch {
//                val uploadResult = viewModel.uploadImage(
//                    uri,
//                    thisActivity.currentUserProfile.username,
//                    requireContext().getFileTypes(listOf(uri))[0]
//                )
//                withContext(Dispatchers.Main) {
//                    uploadResult.observe(viewLifecycleOwner) { url ->
//                        if (url.isEmpty()) {
//                            binding.root.snackbar("Error")
//                        } else {
//                            uploadUrl(profileUrl = url)
//                        }
//                    }
//                    loadingDialog.dismiss()
//                }
//            }
//            Glide.with(requireContext()).load(uri).circleCrop().into(binding.profileImage)
//        }
//    }

//    private val cropBannerImage = registerForActivityResult(cropImagePicker(16f, 9f, 1920)) { uri ->
//        if (uri != null && uri != Uri.EMPTY) {
//            loadingDialog.show()
//            Glide.with(requireContext()).load(uri).into(binding.bgImage)
//            autoDisposeScope.launch {
//                val result = viewModel.uploadImage(
//                    uri,
//                    thisActivity.currentUserProfile.username,
//                    requireActivity().getFileTypes(listOf(uri))[0]
//                )
//                withContext(Dispatchers.Main) {
//                    result.observe(viewLifecycleOwner) { url ->
//                        if (url.isEmpty()) {
//                            binding.root.snackbar("Error")
//                        } else {
//                            uploadUrl(backgroundUrl = url)
//                        }
//                    }
//                    loadingDialog.dismiss()
//                }
//            }
//        }
//    }

//    private val getBannerImageContent =
//        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            if (uri != null) {
//                val outputUri = File(requireActivity().filesDir, "${Date().time}.jpg").toUri()
//                val listUri = listOf(uri, outputUri)
//                cropBannerImage.launch(listUri)
//            }
//        }

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
                } catch (e: Exception) {
                    Uri.EMPTY
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity

        binding.root.setOnRefreshListener {
            binding.root.isRefreshing = false
        }

        val profileTabsAdapter = ProfileTabsAdapter(childFragmentManager, lifecycle)
        binding.profileTabsPager.adapter = profileTabsAdapter

        val tabTexts = arrayListOf("Posts 0", "Tagged")

        TabLayoutMediator(binding.tabItems, binding.profileTabsPager) { tab, position ->
            tab.text = tabTexts[position]
        }.attach()

        sizeLive.observe(viewLifecycleOwner) { size ->
            val displayText =
                "Posts $size".setSpannableColor("$size".formatNumber(), "Posts ".length)
            binding.tabItems.getTabAt(0)?.text = displayText
        }

        val profilePostsCache = MemoryCache.profilePosts
        postsSizeMutable.postValue(profilePostsCache.size)

        viewModel.getProfilePosts(currentUserProfile.identifier)
        viewModel.thumbnails.observe(viewLifecycleOwner) { posts ->
            when (posts) {
                is Resource.Success -> {
                    MemoryCache.profilePosts[currentUserProfile.id] =
                        posts.value.data.toMutableList()
                    postsSizeMutable.postValue(posts.value.data.size)
                    profilePostsMutableCache.postValue(posts.value.data.toMutableList())
                }
                else -> {}
            }
        }

        val username = "@${currentUserProfile.username}"
        binding.profileFullName.text = currentUserProfile.fullname
        binding.profileUsername.text = username

        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(requireView())
                .load(currentUserProfile.profileimg)
                .circleCrop()
                .into(binding.profileImage)

        binding.about.visible(currentUserProfile.about != null && currentUserProfile.about!!.isNotEmpty())
        binding.about.text = currentUserProfile.about
        binding.location.text = currentUserProfile.location

        binding.sendMoneyBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheet = SendMoneyBottomSheetFragment(this)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        followersLive.observe(viewLifecycleOwner) {
            val followersResult = "$it Followers"
            binding.followersCount.text = followersResult.setSpannableColor(it)
        }

        val followers = MemoryCache.profileFollowers[currentUserProfile.id]
        var flw = ""
        if (followers != null) {
            flw = followers
            followersCachedMutable.postValue(flw)
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowersCount(currentUserProfile.id)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (flw != count) {
                            followersCachedMutable.postValue(count)
                            MemoryCache.profileFollowers[currentUserProfile.id] = count
                        }
                    }
                }
                else -> {}
            }
        }

        followingsLive.observe(viewLifecycleOwner) {
            val followingsResult = "$it Following"
            binding.followingCount.text = followingsResult.setSpannableColor(it)
        }

        val followings = MemoryCache.profileFollowings[currentUserProfile.id]
        var flwng = ""
        if (followings != null) {
            flwng = followings
            followingsCachedMutable.postValue(flwng)
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowingsCount(currentUserProfile.id)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (flw != count) {
                            followingsCachedMutable.postValue(count)
                            MemoryCache.profileFollowings[currentUserProfile.id] = count
                        }
                    }
                }
                else -> {}
            }
        }

//        NetworkConnectionLiveData(context ?: return)
//            .observe(viewLifecycleOwner) {
//                loadView()
//            }

//        loadingDialog = Dialog(requireContext())
//        loadingDialog.setCanceledOnTouchOutside(false)
//        val loadingLayoutBinding = LoadingLayoutBinding.inflate(layoutInflater)
//        loadingDialog.setContentView(loadingLayoutBinding.root)
//        val loadingWindow: Window = loadingDialog.window!!
//        loadingWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        loadingWindow.setLayout(
//            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
//        )

//        requireActivity().checkStoragePermissions()
//
//        val dialog = Dialog(requireContext())
//        val selectBinding = SelectImageDialogBinding.inflate(layoutInflater)
//        dialog.setContentView(selectBinding.root)
//        val window: Window = dialog.window!!
//        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        window.setLayout(
//            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//
//        binding.bgImage.setOnClickListener {
//            val request = "Want to upload a new background image?"
//            selectBinding.actionRequest.text = request
//            selectBinding.acceptBtn.setOnClickListener {
//                getBannerImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                dialog.dismiss()
//            }
//            dialog.show()
//        }
//
//        binding.profileImage.setOnClickListener {
//            val request = "Want to upload a new profile photo?"
//            selectBinding.actionRequest.text = request
//            selectBinding.acceptBtn.setOnClickListener {
//                getProfileImageContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                dialog.dismiss()
//            }
//            dialog.show()
//        }
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

//    private fun uploadUrl(backgroundUrl: String? = null, profileUrl: String? = null) {
//        val dto = ProfileDTO(
//            profileImage = profileUrl ?: thisActivity.currentUserProfile.profileimg,
//            backgroundImageUrl = backgroundUrl ?: thisActivity.currentUserProfile.bgimg,
//            phone = thisActivity.currentUserProfile.phone!!,
//            accountType = thisActivity.currentUserProfile.acctype,
//            location = thisActivity.currentUserProfile.location!!,
//            email = thisActivity.currentUserProfile.email,
//            fullName = thisActivity.currentUserProfile.fullname,
//            userName = thisActivity.currentUserProfile.username
//        )
//
//        viewModel.updateProfile(dto)
//        thisActivity.currentUserProfile.bgimg = dto.backgroundImageUrl
//        thisActivity.currentUserProfile.profileimg = dto.profileImage
//
//        lifecycleScope.launch {
//            settingsPreferences.saveProfile(thisActivity.currentUserProfile)
//        }
//    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }

    private fun loadView() {
        lifecycleScope.launch {
            binding.followersCount.showFollowersCount(
                viewModel,
                thisActivity.currentUserProfile
            )
            binding.followingCount.showFollowingCount(
                viewModel,
                thisActivity.currentUserProfile
            )
        }
    }
}