package protrnd.com.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentSearchBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.adapter.ProfileTagAdapter
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.viewholder.ProfileTagViewHolder
import protrnd.com.ui.viewmodels.HomeViewModel
import java.util.Timer
import java.util.TimerTask

class SearchFragment : BaseFragment<HomeViewModel, FragmentSearchBinding, HomeRepository>() {

    private val mutableProfileList = MutableLiveData<MutableList<Profile>>()
    private val liveData: LiveData<MutableList<Profile>> = mutableProfileList

    private val mutablePostsList = MutableLiveData<MutableList<Post>>()
    private val postsLiveData: LiveData<MutableList<Post>> = mutablePostsList

    private lateinit var profileSearchAdapter: ProfileTagAdapter

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSearchBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        val postDatabase = protrndAPIDataSource.providePostDatabase(requireActivity().application)
        val profileDatabase =
            protrndAPIDataSource.provideProfileDatabase(requireActivity().application)
        return HomeRepository(api, postsApi, postDatabase, profileDatabase)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)

        binding.profileSearchResultRv.layoutManager = LinearLayoutManager(requireContext())

        val postsPagingAdapter = PostsPagingAdapter()

        profileSearchAdapter = ProfileTagAdapter(mutableListOf(), showSendBtn = false)
        profileSearchAdapter.setHasStableIds(true)
        binding.profileSearchResultRv.adapter = profileSearchAdapter

        val recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
                    liveData.observe(viewLifecycleOwner) { profileSearchResults ->
                        profileSearchResults.remove(currentUserProfile)
                        if (profileSearchResults.isNotEmpty() || postsPagingAdapter.snapshot().items.isNotEmpty())
                            binding.searchResults.visible(false)
                        else
                            binding.searchResults.visible(true)
                        profileSearchAdapter.setProfileList(profileSearchResults)
                    }
                }
            }
        }

        binding.profileSearchResultRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerViewReadyCallback.onLayoutReady()
                binding.profileSearchResultRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        profileSearchAdapter.clickPosition(object : ProfileClickListener {
            override fun profileClick(
                holder: ProfileTagViewHolder?,
                position: Int,
                profile: Profile
            ) {
                MemoryCache.profiles[profile.id] = profile
                startActivity(Intent(requireContext(), ProfileActivity::class.java).apply {
                    putExtra("profile_id", profile.id)
                })
            }
        })

        binding.postsResultsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.postsResultsRv.adapter = postsPagingAdapter
        postsLiveData.observe(viewLifecycleOwner) { postsSearchResults ->
            if (postsSearchResults.isNotEmpty() || profileSearchAdapter.profiles.isNotEmpty())
                binding.searchResults.visible(false)
            else
                binding.searchResults.visible(true)
            postsPagingAdapter.submitData(lifecycle, PagingData.from(postsSearchResults))
        }

        binding.postsResultsRv.loadPageData(
            childFragmentManager,
            activity as HomeActivity,
            viewModel,
            lifecycleScope,
            requireContext(),
            viewLifecycleOwner,
            currentUserProfile,
            this,
            { removeAlphaVisibility() },
            { showAlpha() },
            token
        )

        viewModel.profiles.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val result = it.value.data.toMutableList()
                    result.remove(currentUserProfile)
                    mutableProfileList.postValue(result)
                    binding.profilesText.visible(result.isNotEmpty())
                    val profilesCount = "${result.size.formatAmount()} Profiles"
                    val split = profilesCount.split(" ")
                    binding.profiles.text = profilesCount.setSpannableColor(split[0])
                }
                is Resource.Loading -> {
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error getting profiles", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {}
            }
        }

        viewModel.posts.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    mutablePostsList.postValue(it.value.data.toMutableList())
                    binding.postsText.visible(it.value.data.isNotEmpty())
                    val postCount = "${it.value.data.size.formatAmount()} Posts"
                    val split = postCount.split(" ")
                    binding.posts.text = postCount.setSpannableColor(split[0])
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error getting posts", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.searchName.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            private val DELAY = 200L

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null || s.toString().isEmpty()) {
                    postsPagingAdapter.submitData(lifecycle, PagingData.from(listOf()))
                    profileSearchAdapter.setProfileList(listOf())
                }
            }

            override fun afterTextChanged(it: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        if (it != null && it.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.searchProfilesByName(it.toString())
                                viewModel.searchPostsByName(it.toString())
                            }
                        } else if (it != null && it.isEmpty()) {
                            postsPagingAdapter.submitData(lifecycle, PagingData.from(listOf()))
                            profileSearchAdapter.setProfileList(listOf())
                        } else {
                            postsPagingAdapter.submitData(lifecycle, PagingData.from(listOf()))
                            profileSearchAdapter.setProfileList(listOf())
                        }
                    }
                }, DELAY)
            }
        })
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }
}