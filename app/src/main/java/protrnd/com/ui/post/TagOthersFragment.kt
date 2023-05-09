package protrnd.com.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Profile
import protrnd.com.data.network.api.SearchApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.SearchRepository
import protrnd.com.databinding.FragmentTagOthersBinding
import protrnd.com.ui.adapter.IncludedProfilesAdapter
import protrnd.com.ui.adapter.ProfileTagAdapter
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewholder.ProfileTagViewHolder
import protrnd.com.ui.viewmodels.SearchViewModel

class TagOthersFragment :
    BaseFragment<SearchViewModel, FragmentTagOthersBinding, SearchRepository>() {
    var profiles = arrayListOf<Profile>()

    override fun getViewModel() = SearchViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTagOthersBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): SearchRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(SearchApi::class.java, token)
        return SearchRepository(api)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)

        val parentFragment = parentFragment as NavHostFragment
        val bottomSheetDialog = parentFragment.parentFragment as TagBottomSheetFragmentDialog
        profiles = bottomSheetDialog.taggedProfiles

        binding.taggedUsersRv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        val includedProfilesAdapter = IncludedProfilesAdapter(profiles)
        binding.taggedUsersRv.adapter = includedProfilesAdapter
        includedProfilesAdapter.removeProfile(object : ProfileClickListener {
            override fun profileClick(
                holder: ProfileTagViewHolder?,
                position: Int,
                profile: Profile
            ) {
                profiles.remove(profile)
                includedProfilesAdapter.notifyItemRemoved(position)
                includedProfilesAdapter.notifyItemRangeChanged(position, profiles.size)
            }
        })

        binding.profilesResultRv.layoutManager = LinearLayoutManager(requireContext())
        var profileSearchResults: MutableList<Profile> = arrayListOf()
        val tagsAdapter = ProfileTagAdapter(profileSearchResults)
        binding.profilesResultRv.adapter = tagsAdapter

        tagsAdapter.clickPosition(object : ProfileClickListener {
            override fun profileClick(
                holder: ProfileTagViewHolder?,
                position: Int,
                profile: Profile
            ) {
                binding.usernameText.text.clear()
                if (!profiles.contains(profile)) {
                    profiles.add(profile)
                    includedProfilesAdapter.notifyItemInserted(profiles.size)
                    profileSearchResults.clear()
                    tagsAdapter.notifyDataSetChanged()
                }
            }
        })

        viewModel.profiles.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    profileSearchResults = it.value.data.toMutableList()
                    profileSearchResults.removeAll(profiles)
                    profileSearchResults.remove(currentUserProfile)
                    tagsAdapter.profiles = profileSearchResults
                    tagsAdapter.notifyDataSetChanged()
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

        binding.usernameText.addTextChangedListener {
            if (it != null && it.isNotEmpty()) {
                viewModel.searchProfilesByName(it.toString())
            }
        }

        binding.doneBtn.setOnClickListener {
            bottomSheetDialog.taggedProfiles = profiles
            bottomSheetDialog.dismiss()
        }
    }
}