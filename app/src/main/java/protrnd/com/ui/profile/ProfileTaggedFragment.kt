package protrnd.com.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.pagingsource.TempPostsPager
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentProfileTaggedBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.home.HomeViewModel

class ProfileTaggedFragment : BaseFragment<HomeViewModel, FragmentProfileTaggedBinding, HomeRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.taggedRv.layoutManager = LinearLayoutManager(requireContext())
        val tempAdapter = TempPostsPager()
        binding.taggedRv.adapter = tempAdapter
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileTaggedBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { settingsPreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}