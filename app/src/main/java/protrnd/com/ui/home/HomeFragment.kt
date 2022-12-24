package protrnd.com.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Location
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentHomeBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.base.BaseFragment

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>() {

    private lateinit var adapter: PostsPagingAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private lateinit var dialog: Dialog
    private val locationHash = HashMap<String, List<String>>()
    private lateinit var thisActivity: HomeActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity
        postsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postsRv.layoutManager = postsLayoutManager

        setupStoredData()
        setupRecyclerView()

        binding.root.setOnRefreshListener {
            if (binding.root.isRefreshing) {
                if (thisActivity.isNetworkAvailable()) {
                    binding.shimmerLayout.visible(true)
                    binding.shimmerLayout.startShimmerAnimation()
                    binding.postsRv.visible(false)
                    loadPage()
                } else {
                    binding.root.snackbar("Please check your network connection")
                }
                binding.root.isRefreshing = false
            }
        }

        dialog = Dialog(requireContext())
        val locationPickerBinding = LocationPickerBinding.inflate(layoutInflater)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        viewModel.locations.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val locations: List<Location> = resource.value.data
                    dialog.setContentView(locationPickerBinding.root)
                    for (location in locations)
                        locationHash[location.state] = location.cities
                    val states = ArrayList<String>()
                    for (state in locationHash.keys)
                        states.add(state)
                    locationPickerBinding.statePicker.setItems(states)
                    dialog.show()
                    val window: Window = dialog.window!!
                    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                is Resource.Loading -> {
                    locationPickerBinding.saveBtn.enable(false)
                }
                is Resource.Failure -> {
                    if (thisActivity.currentUserProfile.location.toString().isEmpty())
                        this.handleAPIError(resource) { lifecycleScope.launch { loadLocations() } }
                }
                else -> {}
            }
        }

        var state = ""
        var city = ""
        locationPickerBinding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            city = ""
            locationPickerBinding.cityPicker.clearSelectedItem()
            state = newItem
            locationPickerBinding.cityPicker.setItems(locationHash[newItem]!!)
        }

        locationPickerBinding.cityPicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            city = newItem
        }

        locationPickerBinding.saveBtn.setOnClickListener {
            if (state.isEmpty() || city.isEmpty())
                Toast.makeText(
                    requireContext(),
                    "Please select a state and city",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                thisActivity.currentUserProfile.location = "$state,$city"
                viewModel.updateProfile(
                    ProfileDTO(
                        profileImage = thisActivity.currentUserProfile.profileimg,
                        backgroundImageUrl = thisActivity.currentUserProfile.bgimg,
                        phone = thisActivity.currentUserProfile.phone!!,
                        accountType = thisActivity.currentUserProfile.acctype,
                        location = thisActivity.currentUserProfile.location!!,
                        email = thisActivity.currentUserProfile.email,
                        fullName = thisActivity.currentUserProfile.fullname,
                        userName = thisActivity.currentUserProfile.username
                    )
                )
                dialog.dismiss()
            }
        }
    }

    private fun loadPage() {
        if (thisActivity.currentUserProfile.location.toString().isEmpty())
            loadLocations()
        //Load first page

        if (requireActivity().isNetworkAvailable()) {
            viewModel.getPostByPage().observe(viewLifecycleOwner) {
                binding.shimmerLayout.stopShimmerAnimation()
                binding.shimmerLayout.visible(false)
                binding.postsRv.visible(true)
                adapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun loadLocations() {
        viewModel.getLocations()
    }

    private fun setupRecyclerView() {
        binding.postsRv.apply {
            adapter = PostsPagingAdapter(
                viewModel = viewModel,
                lifecycleOwner = viewLifecycleOwner,
                currentProfile = thisActivity.currentUserProfile,
                activity = thisActivity
            )
            setItemViewCacheSize(10)
        }
        adapter = binding.postsRv.adapter as PostsPagingAdapter
    }

    private fun setupStoredData() {
        viewModel.getSavedPosts()?.asLiveData()?.observe(viewLifecycleOwner) { saved ->
            if (saved.isNotEmpty()) {
                binding.shimmerLayout.stopShimmerAnimation()
                binding.shimmerLayout.visible(false)
                binding.postsRv.visible(true)
                adapter.submitData(viewLifecycleOwner.lifecycle, PagingData.from(saved))
                if (thisActivity.lmState != null)
                    binding.postsRv.layoutManager!!.onRestoreInstanceState(thisActivity.lmState)
            }
        }

        if (thisActivity.isNetworkAvailable())
            loadPage()
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            val postItems = adapter.snapshot().items
            if (postItems.isNotEmpty())
                viewModel.savePosts(postItems)
            thisActivity.lmState = postsLayoutManager.onSaveInstanceState()!!
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { settingsPreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        val postDatabase = protrndAPIDataSource.providePostDatabase(requireActivity().application)
        val profileDatabase =
            protrndAPIDataSource.provideProfileDatabase(requireActivity().application)
        return HomeRepository(api, postsApi, postDatabase, profileDatabase)
    }

    override fun onDestroy() {
        binding.shimmerLayout.stopShimmerAnimation()
        super.onDestroy()
        if (dialog.isShowing) {
            dialog.cancel()
        }
    }
}