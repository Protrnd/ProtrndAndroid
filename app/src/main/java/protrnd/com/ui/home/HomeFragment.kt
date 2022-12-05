package protrnd.com.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Location
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentHomeBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.ui.adapter.PostsAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.snackbar
import protrnd.com.ui.visible

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>() {

    private lateinit var adapter: PostsAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private var isLoading: Boolean = false
    private var page = 1
    private lateinit var dialog: Dialog
    private val locationHash = HashMap<String,List<String>>()
    private val profileHash = HashMap<String,Profile>()
    lateinit var profile: Profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postsLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, true)

        loadPage()

        binding.root.setOnRefreshListener {
            if (binding.root.isRefreshing) {
                binding.shimmerLayout.visible(true)
                binding.shimmerLayout.startShimmerAnimation()
                binding.postsRv.visible(false)
                page = 1
                loadPage()
                binding.root.isRefreshing = false
            }
        }

        dialog = Dialog(requireContext())
        val locationPickerBinding = LocationPickerBinding.inflate(layoutInflater)
        dialog.setCanceledOnTouchOutside(false)
        viewModel.locations.observe(viewLifecycleOwner) { resource ->
            when(resource) {
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
                    if (resource.isNetworkError)
                        binding.root.snackbar("Error loading locations...please wait while we try again") { lifecycleScope.launch { loadLocations() } }
                    else
                        binding.root.snackbar("User is unauthorized")
                }
            }
        }

        locationPickerBinding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            locationPickerBinding.cityPicker.clearSelectedItem()
            locationPickerBinding.cityPicker.setItems(locationHash[newItem]!!)
        }

        locationPickerBinding.saveBtn.setOnClickListener {
            viewModel.updateProfile(
                ProfileDTO(
                    profileImage = profile.profileimg,
                    backgroundImageUrl = profile.bgimg,
                    phone = profile.phone!!,
                    accountType = profile.acctype,
                    location = profile.location!!,
                    email = profile.email,
                    fullName = profile.fullname,
                    userName = profile.username
                )
            )
            dialog.dismiss()
        }

        binding.root.setOnScrollChangeListener { _, _, _, _, _ ->
            // number of visible items
            val visibleItemCount = postsLayoutManager.childCount
            // number of items in layout
            val totalItemCount = postsLayoutManager.itemCount
            // the position of first visible item
            val firstVisibleItemPosition = postsLayoutManager.findFirstVisibleItemPosition()
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            // validate non negative values
            val isValidFirstItem = firstVisibleItemPosition >= 0
            // validate total items are more than possible visible items
            val totalIsMoreThanVisible = totalItemCount >= 10
            // flag to know whether to load more
            val shouldLoadMore = isValidFirstItem && isAtLastItem && totalIsMoreThanVisible
            if (shouldLoadMore) loadMoreItems(false)
        }
    }

    private fun loadPage() {
        viewModel.getCurrentProfile()
        viewModel.profile.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.postsRv.visible(false)
                }
                is Resource.Success -> {
                    profileHash["profile"] = it.value.data
                    profile = profileHash["profile"]!!
                    if (profile.location == null || profile.location!!.isEmpty())
                        loadLocations()
                    val profile = it.value.data
                    adapter = PostsAdapter(viewModel=viewModel, lifecycleOwner = viewLifecycleOwner, currentProfile = profile)
                    //Load first page
                    loadMoreItems(true)
                }
                is Resource.Failure -> {
                    if (it.isNetworkError) {
                        binding.root.snackbar("Please check your network connection", action = { loadPage() })
                    } else {
                        binding.root.snackbar("User is unauthorized")
                    }
                }
            }
        }
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        if (!isFirstPage)
            page += 1
        viewModel.getPostsPage(page)
        viewModel.postPage.observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch {
                when(it) {
                    is Resource.Success -> {
                        val result = it.value.data
                        if (result.isEmpty())
                            return@launch
                        else if (!isFirstPage) adapter.addAll(result)
                        else adapter.setList(result as MutableList<Post>)
                        binding.shimmerLayout.stopShimmerAnimation()
                        binding.shimmerLayout.visible(false)
                        setupRecyclerView()
                        isLoading = false
                    }
                    is Resource.Loading -> {
                        isLoading = true
                        binding.postsRv.visible(false)
                    }
                    is Resource.Failure -> {
                        isLoading = false
                        if (it.isNetworkError) {
                            binding.root.snackbar("Error occurred") { loadMoreItems(isFirstPage) }
                        } else {
                            binding.root.snackbar("Internal server error occurred!")
                        }
                    }
                }
            }
        }
    }

    private fun loadLocations() {
        viewModel.getLocations()
    }

    private fun setupRecyclerView() {
        binding.postsRv.apply {
            this.adapter = this@HomeFragment.adapter
            this.visible(true)
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmerAnimation()
        dialog.dismiss()
        super.onPause()
    }

    override fun onDestroy() {
        binding.shimmerLayout.stopShimmerAnimation()
        super.onDestroy()
        if (dialog.isShowing){
            dialog.cancel()
        }
    }
}