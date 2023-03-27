package protrnd.com.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Post
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfilePostsBinding
import protrnd.com.ui.adapter.ImageThumbnailPostAdapter
import protrnd.com.ui.addThumbnailGrid3
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.showUserPostsInGrid

class ProfilePostsFragment : BaseFragment<HomeViewModel, FragmentProfilePostsBinding, HomeRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val count = 4
        val posts = listOf(
            Post(uploadurls = arrayListOf("https://static.vecteezy.com/system/resources/thumbnails/002/098/204/small/silver-tabby-cat-sitting-on-green-background-free-photo.jpg")),
            Post(uploadurls = arrayListOf("https://t3.ftcdn.net/jpg/03/31/21/08/360_F_331210846_9yjYz8hRqqvezWIIIcr1sL8UB4zyhyQg.jpg")),
            Post(uploadurls = arrayListOf("https://i.natgeofe.com/n/f0dccaca-174b-48a5-b944-9bcddf913645/01-cat-questions-nationalgeographic_1228126.jpg")),
            Post(uploadurls = arrayListOf("https://storage.googleapis.com/proudcity/santaanaca/uploads/2022/07/Stray-Kittens-scaled.jpg")),
        )
        val postForRV = List(count) {posts}.flatten()
        binding.postThumbnailsRv.addThumbnailGrid3(requireContext(), postForRV)
        val thumbnailAdapter = binding.postThumbnailsRv.adapter as ImageThumbnailPostAdapter

//        thumbnailAdapter.imageClickListener(object : ImagePostItemClickListener {
//            override fun postItemClickListener(post: Post) {
//                startActivity(Intent(requireContext(), PostActivity::class.java).apply {
//                    this.putExtra("post_id", post.identifier)
//                })
//            }
//        })
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfilePostsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { settingsPreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}