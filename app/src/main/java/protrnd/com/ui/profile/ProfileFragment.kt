package protrnd.com.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var flw = ""
    private var flwng = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity

        binding.root.setOnRefreshListener {
            loadData()
            binding.root.isRefreshing = false
        }

        binding.settingsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java).apply {
                thisActivity.startAnimation()
            })
        }

        loadData()

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

        val username = "@${currentUserProfile.username}"
        binding.profileFullName.text = currentUserProfile.fullname
        binding.profileUsername.text = username

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
        if (followers != null) {
            flw = followers
            followersCachedMutable.postValue(flw)
        }


        followingsLive.observe(viewLifecycleOwner) {
            val followingsResult = "$it Following"
            binding.followingCount.text = followingsResult.setSpannableColor(it)
        }

        val followings = MemoryCache.profileFollowings[currentUserProfile.id]
        if (followings != null) {
            flwng = followings
            followingsCachedMutable.postValue(flwng)
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

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }

    fun loadData() {
        if (currentUserProfile.profileimg.isNotEmpty())
            Glide.with(requireView())
                .load(currentUserProfile.profileimg)
                .circleCrop()
                .into(binding.profileImage)

        if (currentUserProfile.bgimg.isNotEmpty())
            Glide.with(requireView())
                .load(currentUserProfile.bgimg)
                .into(binding.bgimg)

        viewModel.getProfilePosts(currentUserProfile.identifier)

        CoroutineScope(Dispatchers.IO).launch {
            when (val fc = viewModel.getFollowingsCount(currentUserProfile.id)) {
                is Resource.Success -> {
                    if (fc.value.successful) {
                        val count = "${fc.value.data}".formatNumber()
                        if (flwng != count) {
                            followingsCachedMutable.postValue(count)
                            MemoryCache.profileFollowings[currentUserProfile.id] = count
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
                        if (flw != count) {
                            followersCachedMutable.postValue(count)
                            MemoryCache.profileFollowers[currentUserProfile.id] = count
                        }
                    }
                }
                else -> {}
            }
        }
    }
}