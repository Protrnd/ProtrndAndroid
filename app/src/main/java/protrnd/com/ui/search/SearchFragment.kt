package protrnd.com.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

        liveData.observe(viewLifecycleOwner) { profileSearchResults ->
            profileSearchResults.remove(currentUserProfile)
            binding.searchResults.visible(profileSearchResults.isEmpty() && postsPagingAdapter.itemCount <= 0)
            profileSearchAdapter = ProfileTagAdapter(profileSearchResults, showSendBtn = false)
            binding.profileSearchResultRv.adapter = profileSearchAdapter
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
        }

        binding.postsResultsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.postsResultsRv.adapter = postsPagingAdapter
        postsLiveData.observe(viewLifecycleOwner) { postsSearchResults ->
            binding.searchResults.visible(postsSearchResults.isEmpty() && profileSearchAdapter.profiles.isEmpty())
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
                    mutableProfileList.postValue(it.value.data.toMutableList())
                    binding.profilesText.visible(it.value.data.isNotEmpty())
                    val profilesCount = "${it.value.data.size.formatAmount()} Profiles"
                    val split = profilesCount.split(" ")
                    binding.profiles.text = profilesCount.setSpannableColor(split[0])
                }
                is Resource.Loading -> {
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error getting profiles", Toast.LENGTH_SHORT)
                        .show()
                }
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

        binding.searchName.addTextChangedListener {
            if (it != null && it.isNotEmpty()) {
                viewModel.searchProfilesByName(it.toString())
                viewModel.searchPostsByName(it.toString())
            }
        }
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }
}