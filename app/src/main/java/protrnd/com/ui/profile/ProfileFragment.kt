package protrnd.com.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfileBinding
import protrnd.com.ui.adapter.ProfileTabsAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.formatNumber
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.setSpannableColor
import protrnd.com.ui.settings.SettingsActivity
import protrnd.com.ui.startAnimation
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.visible
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment

class ProfileFragment : BaseFragment<HomeViewModel, FragmentProfileBinding, HomeRepository>() {

    private lateinit var thisActivity: HomeActivity
    private val postsSizeMutable = MutableLiveData<Int>()
    private val sizeLive: LiveData<Int> = postsSizeMutable
    private val followersCachedMutable = MutableLiveData<String>()
    private val followersLive: LiveData<String> = followersCachedMutable
    private val followingsCachedMutable = MutableLiveData<String>()
    private val followingsLive: LiveData<String> = followingsCachedMutable
    private val profilePostsMutableCache = MutableLiveData<MutableList<Post>>()
    val profilePostsLive: LiveData<MutableList<Post>> = profilePostsMutableCache
    private val profileMutable = MutableLiveData<Profile>()
    private val profileLive: LiveData<Profile> = profileMutable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity

        binding.root.setOnRefreshListener {
            loadData()
        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java).apply {
                thisActivity.startAnimation()
            })
        }

        followersLive.observe(viewLifecycleOwner) {
            val followersResult = "$it Followers"
            binding.followersCount.text = followersResult.setSpannableColor(it)
        }

        followersCachedMutable.postValue(followers)

        followingsLive.observe(viewLifecycleOwner) {
            val followingsResult = "$it Following"
            binding.followingCount.text = followingsResult.setSpannableColor(it)
        }

        followingsCachedMutable.postValue(followings)

        loadData()

        profileLive.observe(viewLifecycleOwner) {
            val username = "@${it.username}"
            binding.profileFullName.text = it.fullname
            binding.profileUsername.text = username

            binding.about.visible(it.about != null && it.about!!.isNotEmpty())
            binding.about.text = it.about
            binding.location.text = it.location
            if (it.profileimg.isNotEmpty())
                Glide.with(requireContext())
                    .load(it.profileimg)
                    .circleCrop()
                    .into(binding.profileImage)

            if (it.bgimg.isNotEmpty())
                Glide.with(requireContext())
                    .load(it.bgimg)
                    .into(binding.bgimg)
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

        binding.sendMoneyBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val bottomSheet = SendMoneyBottomSheetFragment(this)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val profile = runBlocking { profilePreferences.profile.first() }
        currentUserProfile = Gson().fromJson(profile, Profile::class.java)
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val prof = profilePreferences.profile.first()
            if (prof != null) {
                currentUserProfile = Gson().fromJson(prof, Profile::class.java)
                profileMutable.postValue(currentUserProfile)
            }
        }

        viewModel.getProfilePosts(currentUserProfile.identifier)

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowingsCount(currentUserProfile.id)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (followings != count) {
                            followingsCachedMutable.postValue(count)
                            followings = count
                            profilePreferences.saveFollowings(followings)
                        }
                    }
                }
                else -> {}
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowersCount(currentUserProfile.id)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (followers != count) {
                            followersCachedMutable.postValue(count)
                            followers = count
                            profilePreferences.saveFollowers(followers)
                        }
                    }
                }
                else -> {}
            }
        }

        if (binding.root.isRefreshing)
            binding.root.isRefreshing = false
    }
}