package protrnd.com.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityPostBinding
import protrnd.com.databinding.BottomSheetCommentsBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewmodels.HomeViewModel
import java.util.concurrent.Executors

class PostActivity : BaseActivity<ActivityPostBinding, HomeViewModel, HomeRepository>() {
    private var post: Post? = null
    private var postMap: HashMap<String, Post> = HashMap()
    private var postId = ""
    private var isFromNotification = false
    private val otherProfileHash = HashMap<String, Profile>()
    private val postLiveData = MutableLiveData<Post>()
    private val postLive: LiveData<Post> = postLiveData
    private val otherProfileLiveData = MutableLiveData<Profile>()
    private val otherProfile: LiveData<Profile> = otherProfileLiveData

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras!!
            postId = bundle.getString("post_id").toString()
            if (bundle.containsKey("isFromNotification")) {
                isFromNotification = bundle.getBoolean("isFromNotification")
            }
        }

        lifecycleScope.launch {
            loadPostData()
        }

        binding.navBackBtn.setOnClickListener {
            goBack()
        }

        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(applicationContext).load(currentUserProfile.profileimg)
                .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()
                .into(binding.navImage)
        binding.navName.text = currentUserProfile.username

        binding.likeToggle.setOnClickListener {
            likePost(
                binding.likeToggle,
                binding.likesCount,
                viewModel,
                postId,
                otherProfileHash["otherProfile"]!!,
                currentUserProfile
            )
        }

        binding.sendTextBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val forwardPostDialog = ForwardPostBottomSheetDialog(
                currentUserProfile,
                post!!,
                authToken!!,
                activity = this
            )
            forwardPostDialog.show(supportFragmentManager, forwardPostDialog.tag)
        }

        binding.promoteSupport.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheetSupport = SupportBottomSheet(activity = this, post = post!!)
            bottomSheetSupport.show(supportFragmentManager, bottomSheetSupport.tag)
        }

        binding.commentBtn.setOnClickListener {
            if (post != null) {
                binding.alphaBg.visible(true)
                val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetTheme)
                val bottomSheetBinding =
                    BottomSheetCommentsBinding.inflate(layoutInflater)
                bottomSheet.setContentView(bottomSheetBinding.root)
                bottomSheet.setCanceledOnTouchOutside(true)
                bottomSheetBinding.commentSection.layoutManager =
                    LinearLayoutManager(this)
                bottomSheetBinding.commentsCount.text =
                    bottomSheetBinding.commentsCount.text.toString().setSpannableColor(
                        bottomSheetBinding.commentsCount.text.toString().replace("Comments", ""), 8
                    )
                val timeAgo = post!!.time.getAgo()
                bottomSheetBinding.timeAgo.text = timeAgo

                bottomSheet.setOnCancelListener {
                    binding.alphaBg.visible(false)
                }
                bottomSheet.setOnDismissListener {
                    binding.alphaBg.visible(false)
                }
                val commentRecyclerViewReadyCallback = object : RecyclerViewReadyCallback {
                    override fun onLayoutReady() {
                        NetworkConnectionLiveData(this@PostActivity).observe(
                            this@PostActivity
                        ) {
                            val executor = Executors.newFixedThreadPool(5)
                            executor.execute {
                                lifecycleScope.launch {
                                    val profileResult = getOtherProfile(post!!.profileid)
                                    if (profileResult != null) {
                                        val postedBy = "Posted by ${profileResult.username}"
                                        bottomSheetBinding.postedBy.text = postedBy

                                        bottomSheetBinding.showCommentSection(
                                            viewModel,
                                            this@PostActivity,
                                            lifecycleScope,
                                            profileResult,
                                            currentUserProfile,
                                            post!!.identifier
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                bottomSheetBinding.commentSection.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        commentRecyclerViewReadyCallback.onLayoutReady()
                        bottomSheetBinding.commentSection.viewTreeObserver.removeOnGlobalLayoutListener(
                            this
                        )
                    }
                })
                bottomSheet.show()
            } else {
                binding.root.errorSnackBar("Error occurred when loading post!")
            }
        }

        postLive.observe(this) { result ->
            postMap["post"] = result
            post = postMap["post"]
            if (post!!.profileid == currentUserProfile.id) {
                binding.promoteSupport.visibility = View.INVISIBLE
                binding.promoteSupport.isEnabled = false
            }
            binding.postContent.visible(true)
        }

        otherProfile.observe(this) {
            otherProfileHash["otherProfile"] = it
            bindPostDetails(
                tabLayout = binding.tabLayout,
                fullnameTv = binding.fullname,
                locationTv = binding.location,
                captionTv = binding.captionTv,
                post = post!!,
                profileImage = binding.postOwnerImage,
                imagesPager = binding.imagesViewPager,
                postOwnerProfile = it,
                timeText = binding.time,
                activity = this,
                viewModel = viewModel,
                currentProfile = currentUserProfile
            )
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityPostBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    private suspend fun loadPostData() {
        val postResult = MemoryCache.posts.firstOrNull { post -> post.id == postId }
        if (postResult != null) {
            val post: Post = postResult
            postLiveData.postValue(post)
            val profileResult = MemoryCache.profiles[post.profileid]
            if (profileResult != null) {
                val profile: Profile = profileResult
                otherProfileLiveData.postValue(profile)
            }
        }

        when (val item = viewModel.getPost(postId)) {
            is Resource.Success -> {
                if (!MemoryCache.posts.contains(item.value.data)) {
                    postLiveData.postValue(item.value.data)
                    MemoryCache.posts.add(item.value.data)
                }

                when (val otherProfile = viewModel.getProfileById(item.value.data.profileid)) {
                    is Resource.Success -> {
                        otherProfileLiveData.postValue(otherProfile.value.data)
                        MemoryCache.profiles[item.value.data.profileid] = otherProfile.value.data
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        binding.root.errorSnackBar("An error occurred, please try again")
                    }
                }
            }
            is Resource.Loading -> {
                binding.postContent.visible(false)
            }
            is Resource.Failure -> {
                binding.root.errorSnackBar("An error occurred, please try again")
            }
        }

        viewModel.setupLikes(
            postId,
            binding.likesCount,
            binding.likeToggle,
            this
        )
    }

    private fun getStoredProfile(id: String): Profile? {
        val profileResult = MemoryCache.profiles[id]
        if (profileResult != null)
            return profileResult
        val otherProfile = viewModel.getProfile(id)
        var result: Profile? = null
        otherProfile?.asLiveData()?.observe(this) {
            if (it != null) {
                result = it
            }
        }
        return result
    }

    private suspend fun getOtherProfile(
        id: String
    ): Profile? {
        return getStoredProfile(id) ?: when (val otherProfile = viewModel.getProfileById(id)) {
            is Resource.Success -> {
                viewModel.saveProfile(otherProfile.value.data)
                MemoryCache.profiles[id] = otherProfile.value.data
                return otherProfile.value.data
            }
            is Resource.Loading -> {
                return null
            }
            is Resource.Failure -> {
                return null
            }
            else -> {
                return null
            }
        }
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    private fun goBack() {
        if (isFromNotification)
            startNewActivityWithNoBackstack(HomeActivity::class.java)
        else
            finishActivity()
    }
}