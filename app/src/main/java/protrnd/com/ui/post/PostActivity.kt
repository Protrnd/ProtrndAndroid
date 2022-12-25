package protrnd.com.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityPostBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeViewModel

class PostActivity : BaseActivity<ActivityPostBinding, HomeViewModel, HomeRepository>() {
    private var post: Post? = null
    private var postMap: HashMap<String, Post> = HashMap()
    private var postId = ""
    private val otherProfileHash = HashMap<String, Profile>()

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        binding.navBackBtn.setOnClickListener {
            if (post != null) {
                val returnIntent = Intent()
                returnIntent.putExtra("profile_id", post!!.profileid)
                setResult(RESULT_OK, returnIntent)
            }
            finishActivity()
        }

        postId = intent?.getStringExtra("post_id")!!

        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(applicationContext).load(currentUserProfile.profileimg).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).circleCrop()
                .into(binding.navImage)
        binding.navName.text = currentUserProfile.username

        binding.likeToggle.setOnClickListener {
            likePost(
                binding.likeToggle,
                binding.likesCount,
                lifecycleScope,
                viewModel,
                postId,
                otherProfileHash["otherProfile"]!!,
                currentUserProfile
            )
        }

        lifecycleScope.launch {
            loadPostData()
        }

        binding.commentBtn.setOnClickListener {
            this.showCommentSection(
                viewModel,
                this,
                lifecycleScope,
                otherProfileHash["otherProfile"]!!,
                currentUserProfile,
                postId
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
        when (val item = viewModel.getPost(postId)) {
            is Resource.Success -> {
                when (val otherProfile = viewModel.getProfileById(item.value.data.profileid)) {
                    is Resource.Success -> {
                        val result = item.value.data
                        postMap["post"] = result
                        post = postMap["post"]
                        otherProfileHash["otherProfile"] = otherProfile.value.data
                        binding.bindPostDetails(
                            usernameTv = binding.username,
                            fullnameTv = binding.fullname,
                            locationTv = binding.location,
                            captionTv = binding.captionTv,
                            post = post!!,
                            profileImage = binding.postOwnerImage,
                            imagesPager = binding.imagesViewPager,
                            postOwnerProfile = otherProfile.value.data,
                            tabLayout = binding.tabLayout,
                            timeText = binding.timeUploaded,
                            activity = this
                        )
                        binding.postResult.visible(true)
                        binding.shimmerLayout.visible(false)
                    }
                    is Resource.Failure -> {
                        binding.root.snackbar("An error occurred, please try again") { lifecycleScope.launch { loadPostData() } }
                    }
                    else -> {
                        binding.postResult.visible(false)
                        binding.shimmerLayout.visible(true)
                    }
                }
            }
            else -> {
                binding.postResult.visible(false)
                binding.shimmerLayout.visible(true)
            }
        }

        setupLikes(
            viewModel,
            postId,
            this,
            binding.likesCount,
            binding.likeToggle
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        postId = intent?.getStringExtra("post_id")!!
        lifecycleScope.launch {
            loadPostData()
        }
    }
}