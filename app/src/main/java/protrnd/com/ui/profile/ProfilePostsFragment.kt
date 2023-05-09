package protrnd.com.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfilePostsBinding
import protrnd.com.ui.addThumbnailGrid4
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.HomeViewModel

class ProfilePostsFragment :
    BaseFragment<HomeViewModel, FragmentProfilePostsBinding, HomeRepository>() {

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)
        when (val activity = activity) {
            is ProfileActivity -> {
                activity.profilePostsLive.observe(viewLifecycleOwner) { posts ->
                    binding.thumbnailsRv.addThumbnailGrid4(requireContext(), posts)
                }
            }
            else -> {
                val parentFrag = requireParentFragment() as ProfileFragment
                parentFrag.profilePostsLive.observe(viewLifecycleOwner) { posts ->
                    binding.thumbnailsRv.addThumbnailGrid4(requireContext(), posts)
                }
            }
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfilePostsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}