package protrnd.com.ui.post

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import protrnd.com.R
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityPostBinding
import protrnd.com.databinding.BottomSheetCommentsBinding
import protrnd.com.ui.adapter.CommentsAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.bindPostDetails
import protrnd.com.ui.enable
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.snackbar
import protrnd.com.ui.visible

class PostActivity : BaseActivity<ActivityPostBinding, HomeViewModel, HomeRepository>() {
    private var profile: Profile? = null
    private var profileMap: HashMap<String, Profile> = HashMap()
    private var post: Post? = null
    private var postMap: HashMap<String, Post> = HashMap()
    private var postId = ""

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        binding.navBackBtn.setOnClickListener {
            if (post != null) {
                val returnIntent = Intent()
                returnIntent.putExtra("profile_id", post!!.profileid)
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
        postId = intent?.getStringExtra("post_id")!!

        loadProfile()

        viewModel.profile.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    binding.shimmerLayout.visible(true)
                    binding.postResult.visible(false)
                }
                is Resource.Success -> {
                    val result = it.value.data
                    profileMap["profile"] = result
                    profile = profileMap["profile"]
                    if (profile?.profileimg!!.isNotEmpty())
                        Glide.with(this).load(profile!!.profileimg).circleCrop()
                            .into(binding.navImage)
                    binding.navName.text = profile!!.username

                }
                is Resource.Failure -> {
                    if (it.isNetworkError) {
                        binding.root.snackbar("Please check your network connection", action = { loadProfile() })
                    }
                }
            }
        }

        binding.likeToggle.setOnClickListener {
            val liked = binding.likeToggle.isChecked
            var likesResult = binding.likesCount.text.toString()
            likesResult = if (likesResult.contains("likes"))
                likesResult.replace(" likes", "")
            else
                likesResult.replace(" like", "")
            var count = likesResult.toInt()
            lifecycleScope.launch {
                if (liked) {
                    count += 1
                    val likes = if (count > 1) "$count likes" else "$count like"
                    binding.likesCount.text = likes
                    when (viewModel.likePost(postId)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                } else {
                    count -= 1
                    val likes = if (count > 1) "$count likes" else "$count like"
                    binding.likesCount.text = likes
                    when (viewModel.unlikePost(postId)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            loadPostData()
        }

        binding.commentBtn.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetTheme)
            val bottomSheetBinding = BottomSheetCommentsBinding.inflate(LayoutInflater.from(this))
            bottomSheet.setContentView(bottomSheetBinding.root)
            bottomSheetBinding.commentSection.layoutManager = LinearLayoutManager(this)
            loadComments()
            viewModel.comments.observe(this) { comments ->
                when(comments) {
                    is Resource.Success -> {
                        if (comments.value.data.isNotEmpty()) {
                            bottomSheetBinding.commentSection.visible(true)
                            bottomSheetBinding.noCommentsTv.visible(false)
                            val commentsText = "${comments.value.data.size} Comments"
                            bottomSheetBinding.commentsCount.text = commentsText
                            val commentAdapter = CommentsAdapter(
                                viewModel = viewModel,
                                comments = comments.value.data
                            )
                            bottomSheetBinding.commentSection.adapter = commentAdapter
                        }
                    }
                    is Resource.Failure -> {
                        if (comments.isNetworkError) {
                            bottomSheetBinding.root.snackbar("Error loading comments") { loadComments() }
                        }
                    }
                    else -> {}
                }
            }

            bottomSheetBinding.sendComment.setOnClickListener {
                val commentContent = bottomSheetBinding.commentInput.text.toString().trim()
                if (commentContent.isNotEmpty()) {
                    bottomSheetBinding.sendComment.enable(false)
                    val comment = CommentDTO(comment = commentContent, postid = postId)
                    lifecycleScope.launch {
                        when (val result = viewModel.addComment(comment)) {
                            is Resource.Success -> {
                                bottomSheetBinding.sendComment.enable(true)
                                if (result.value.successful) {
                                    bottomSheetBinding.commentInput.text.clear()
                                    viewModel.getComments(postId)
                                }
                            }
                            is Resource.Loading -> {
                                bottomSheetBinding.sendComment.enable(false)
                            }
                            is Resource.Failure -> {
                                bottomSheetBinding.sendComment.enable(true)
                            }
                        }
                    }
                } else{
                    bottomSheetBinding.inputField.error = "This field cannot be empty"
                }
            }
            val frame = bottomSheet.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behaviour = BottomSheetBehavior.from(frame)
            val layoutparams = frame.layoutParams
            val windowHeight = Resources.getSystem().displayMetrics.heightPixels
            if (layoutparams != null)
                layoutparams.height = windowHeight
            frame.layoutParams = layoutparams
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheet.show()
            bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

    private fun loadProfile() {
        viewModel.getCurrentProfile()
    }

    private suspend fun loadPostData() {
        when (val item = viewModel.getPost(postId)) {
            is Resource.Success -> {
                when (val otherProfile = viewModel.getProfileById(item.value.data.profileid)) {
                    is Resource.Success -> {
                        val result = item.value.data
                        postMap["post"] = result
                        post = postMap["post"]
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
                            timeText = binding.timeUploaded
                        )
                        binding.postResult.visible(true)
                        binding.shimmerLayout.visible(false)
                    }
                    is Resource.Failure -> {
                        binding.root.snackbar("An error occurred we will try again") { lifecycleScope.launch { loadPostData() } }
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

        when (val likesCount = viewModel.getLikesCount(postId)) {
            is Resource.Success -> {
                val count = likesCount.value.data as Double
                val likes = if (count > 1) "${count.toInt()} likes" else "${count.toInt()} like"
                binding.likesCount.text = likes
            }
            else -> {}
        }

        val isLiked = viewModel.postIsLiked(postId)
        withContext(Dispatchers.Main) {
            isLiked.observe(this@PostActivity) {
                when (it) {
                    is Resource.Success -> {
                        binding.likeToggle.isChecked = it.value.data
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadComments() {
        viewModel.getComments(postId)
    }
}