package protrnd.com.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityProfileBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ProfileTabsAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.chat.ChatContentActivity
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment

class ProfileActivity : BaseActivity<ActivityProfileBinding, HomeViewModel, HomeRepository>() {
    private val postsSizeMutable = MutableLiveData<Int>()
    private val sizeLive: LiveData<Int> = postsSizeMutable
    private val followersCachedMutable = MutableLiveData<String>()
    private val followersLive: LiveData<String> = followersCachedMutable
    private val followingsCachedMutable = MutableLiveData<String>()
    private val followingsLive: LiveData<String> = followingsCachedMutable
    private val profilePostsMutableCache = MutableLiveData<MutableList<Post>>()
    val profilePostsLive: LiveData<MutableList<Post>> = profilePostsMutableCache
    private val profileMutable: MutableLiveData<Profile> = MutableLiveData()
    private val profileLive: LiveData<Profile> = profileMutable
    var firstInstance = true
    var profileId = ""
    var followersCount = 0

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = false
        }

        profileId = intent!!.getStringExtra("profile_id")!!

        viewModel.isFollowing(profileId)

        viewModel.isFollowing.observe(this) {
            when (it) {
                is Resource.Success -> {
                    binding.followBtn.isChecked = it.value.data.toString().toBoolean()
                    firstInstance = false
                }
                else -> {}
            }
        }

        binding.messageBtn.setOnClickListener {
            startActivity(Intent(this, ChatContentActivity::class.java).apply {
                putExtra("profileid", profileId)
            })
        }

        binding.navBackBtn.setOnClickListener {
            finishActivity()
        }

        binding.followBtn.setOnCheckedChangeListener { _, isChecked ->
            if (!firstInstance) {
                if (isChecked) {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.follow(profileId)
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.unfollow(profileId)
                    }
                }
                val a = if (isChecked) followersCount + 1 else followersCount - 1
                followersCount = a
                followersCachedMutable.postValue(a.formatAmount())
            }
        }

        val profileTabsAdapter = ProfileTabsAdapter(supportFragmentManager, lifecycle)
        binding.profileTabsPager.adapter = profileTabsAdapter

        binding.navName.text = currentUserProfile.username
        val tabTexts = arrayListOf("Posts 0", "Tagged")

        TabLayoutMediator(binding.tabItems, binding.profileTabsPager) { tab, position ->
            tab.text = tabTexts[position]
        }.attach()

        sizeLive.observe(this) { size ->
            val displayText =
                "Posts $size".setSpannableColor("$size".formatNumber(), "Posts ".length)
            binding.tabItems.getTabAt(0)?.text = displayText
        }

        val profilePostsCache = MemoryCache.profilePosts
        postsSizeMutable.postValue(profilePostsCache.size)

        viewModel.getProfilePosts(profileId)
        viewModel.thumbnails.observe(this) { posts ->
            when (posts) {
                is Resource.Success -> {
                    MemoryCache.profilePosts[profileId] = posts.value.data.toMutableList()
                    postsSizeMutable.postValue(posts.value.data.size)
                    profilePostsMutableCache.postValue(posts.value.data.toMutableList())
                }
                else -> {}
            }
        }

        val profile = MemoryCache.profiles[profileId]
        if (profile != null) {
            val result: Profile = profile
            profileMutable.postValue(result)
        }

        profileLive.observe(this) { profileData ->
            val username = "@${profileData.username}"
            binding.profileFullName.text = profileData.fullname
            binding.profileUsername.text = username

            if (profileData.profileimg.isNotEmpty())
                Glide.with(this)
                    .load(profileData.profileimg)
                    .circleCrop()
                    .into(binding.profileImage)

            binding.about.visible(profileData.about != null && profileData.about!!.isNotEmpty())
            binding.about.text = profileData.about
            binding.location.text = profileData.location

            binding.sendMoneyBtn.setOnClickListener {
                binding.alphaBg.visible(true)
                val bottomSheet =
                    SendMoneyBottomSheetFragment(activity = this, profile = profileData)
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }

        followersLive.observe(this) {
            val followersResult = "$it Followers"
            binding.followersCount.text = followersResult.setSpannableColor(it)
        }

        val followers = MemoryCache.profileFollowers[profileId]
        var flw = ""
        if (followers != null) {
            flw = followers
            followersCachedMutable.postValue(flw)
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowersCount(profileId)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        followersCount = "${fc.value.data}".toInt()
                        val count = "$followersCount".formatNumber()
                        if (flw != count) {
                            followersCachedMutable.postValue(count)
                            MemoryCache.profileFollowers[profileId] = count
                        }
                    }
                }
                else -> {}
            }
        }

        followingsLive.observe(this) {
            val followingsResult = "$it Following"
            binding.followingCount.text = followingsResult.setSpannableColor(it)
        }

        val followings = MemoryCache.profileFollowings[profileId]
        if (followings != null) {
            val flwng: String = followings
            followingsCachedMutable.postValue(flwng)
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowingsCount(profileId)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (flw != count) {
                            followingsCachedMutable.postValue(count)
                            MemoryCache.profileFollowings[profileId] = count
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityProfileBinding.inflate(inflater)

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}