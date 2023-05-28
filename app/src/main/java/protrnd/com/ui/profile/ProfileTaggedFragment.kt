package protrnd.com.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfileTaggedBinding
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.errorSnackBar
import protrnd.com.ui.loadPageData
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.visible

class ProfileTaggedFragment :
    BaseFragment<HomeViewModel, FragmentProfileTaggedBinding, HomeRepository>() {
    private lateinit var adapter: PostsPagingAdapter
    var userId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.taggedRv.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostsPagingAdapter()
        binding.taggedRv.adapter = adapter
        when (val parent = activity) {
            is ProfileActivity -> {
                userId = parent.profileId
                binding.taggedRv.loadPageData(
                    childFragmentManager,
                    activity as BaseActivity<*, *, *>,
                    viewModel,
                    lifecycleScope,
                    requireContext(),
                    viewLifecycleOwner,
                    currentUserProfile,
                    this,
                    { parent.removeAlphaVisibility() },
                    { parent.showAlpha() },
                    token
                )
            }
            else -> {
                userId = currentUserProfile.id
                val pf = parentFragment as ProfileFragment
                binding.taggedRv.loadPageData(
                    childFragmentManager,
                    activity as BaseActivity<*, *, *>,
                    viewModel,
                    lifecycleScope,
                    requireContext(),
                    viewLifecycleOwner,
                    currentUserProfile,
                    this,
                    { pf.removeAlphaVisibility() },
                    { pf.showAlpha() },
                    token
                )
            }
        }

        loadPage()
    }

    private fun loadPage() {
        requestPosts()
    }

    private fun requestPosts() {
        viewModel.getProfilePostTagsPage(userId).observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    adapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {
                            binding.shimmer.visible(true)
                        } else {
                            binding.shimmer.visible(false)
                            if (adapter.itemCount < 1) {
                                binding.root.errorSnackBar("Error loading posts") { loadPage() }
                            } else {
                                // TODO: Network error
                            }
                        }
                    }
                }
            }
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileTaggedBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}