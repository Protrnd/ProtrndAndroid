package protrnd.com.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityProfileBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ImageThumbnailPostAdapter
import protrnd.com.ui.adapter.listener.ImagePostItemClickListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.post.PostActivity

class ProfileActivity : BaseActivity<ActivityProfileBinding,HomeViewModel,HomeRepository>() {
    private var profile: Profile? = null
    private var profileMap: HashMap<String, Profile> = HashMap()
    private var profileId: String = ""

    val profileIdResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            profileId = it.data!!.getStringExtra("profile_id").toString()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        binding.navBackBtn.setOnClickListener { finish() }
        if (profileId.isEmpty())
            profileId = intent?.getStringExtra("profile_id").toString()

        lifecycleScope.launch {
            when(val result = viewModel.getProfileById(profileId)) {
                is Resource.Success -> {
                    profileMap["profile"] = result.value.data
                    profile = profileMap["profile"]
                    binding.profileFullName.text = profile?.fullname
                    val username = "@${profile?.username.toString()}"
                    binding.profileUsername.text = username
                    binding.navName.text = username.replace("@","")

                    viewModel.isFollowing(profileId)
                    viewModel.isFollowing.observe(this@ProfileActivity){
                        when(it){
                            is Resource.Success -> {
                                binding.followToggle.enable(true)
                                binding.followToggle.isChecked = it.value.data as Boolean
                            }
                            is Resource.Loading -> {
                                binding.followToggle.enable(false)
                            }
                            is Resource.Failure -> {
                                if (it.errorCode == 404) {
                                    binding.followToggle.visible(false)
                                }
                            }
                            else -> {}
                        }
                    }

                    viewModel.getCurrentProfile()
                    viewModel.profile.observe(this@ProfileActivity){ currentUser ->
                        when(currentUser) {
                            is Resource.Success -> {
                                if (currentUser.value.data.identifier == profileId)
                                    binding.followToggle.visible(false)
                                else
                                    binding.followToggle.visible(true)
                            }
                            else -> {}
                        }
                    }

                    if (profile?.profileimg!!.isNotEmpty()) {
                        Glide.with(applicationContext)
                            .load(profile?.profileimg).circleCrop().into(binding.profileImage)
                        Glide.with(applicationContext)
                            .load(profile?.profileimg).circleCrop().into(binding.navImage)
                    }

                    if (profile?.bgimg!!.isNotEmpty())
                        Glide.with(applicationContext)
                            .load(profile?.bgimg).into(binding.bgImage)

                    lifecycleScope.launch {
                        binding.followersCount.showFollowersCount(viewModel, profile!!)
                        binding.followingCount.showFollowingCount(viewModel, profile!!)
                        binding.postsRv.showUserPostsInGrid(this@ProfileActivity,viewModel, profile!!)
                        val thumbnailAdapter = binding.postsRv.adapter as ImageThumbnailPostAdapter
                        thumbnailAdapter.imageClickListener(object : ImagePostItemClickListener{
                            override fun postItemClickListener(post: Post) {
                                profileIdResult.launch(Intent(this@ProfileActivity,PostActivity::class.java).apply {
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
                    binding.root.snackbar("Error loading page") { finish() }
                }
            }
        }

        binding.followToggle.setOnClickListener {
            val following = binding.followToggle.isChecked
            if (following) {
                lifecycleScope.launch {
                    when(viewModel.follow(profileId)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                }
            } else {
                lifecycleScope.launch {
                    when(viewModel.unfollow(profileId)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                }
            }
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) = ActivityProfileBinding.inflate(inflater)

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}