package protrnd.com.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Location
import protrnd.com.data.models.Post
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
import protrnd.com.ui.handleAPIError
import protrnd.com.ui.visible

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>() {

    private lateinit var adapter: PostsAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private var isLoading: Boolean = false
    private var page = 1
    private lateinit var dialog: Dialog
    private val locationHash = HashMap<String, List<String>>()
    private lateinit var thisActivity: HomeActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity
        postsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postsRv.layoutManager = postsLayoutManager

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
                    this.handleAPIError(resource) { lifecycleScope.launch { loadLocations() } }
                }
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

        binding.postsRv.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!isLoading) {
                loadMoreItems(false)
                isLoading = true
            }
        }
    }

    private fun loadPage() {
        if (thisActivity.currentUserProfile.location == null || thisActivity.currentUserProfile.location!!.isEmpty())
            loadLocations()
        adapter = PostsAdapter(
            viewModel = viewModel,
            lifecycleOwner = viewLifecycleOwner,
            currentProfile = thisActivity.currentUserProfile
        )
        //Load first page
        setupRecyclerView()
        loadMoreItems(true)
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        if (!isFirstPage)
            page += 1
        viewLifecycleOwner.lifecycleScope.launch {
            when (val posts = viewModel.getPostByPage(page)) {
                is Resource.Success -> {
                    val result = posts.value.data
                    if (result.isEmpty())
                        return@launch
                    if (!isFirstPage) adapter.addAll(result)
                    else adapter.setList(result as MutableList<Post>)
                    isLoading = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.shimmerLayout.stopShimmerAnimation()
                        binding.shimmerLayout.visible(false)
                        binding.postsRv.visible(true)
                    }, 5000)
                }
                is Resource.Loading -> {
                    isLoading = true
                    binding.postsRv.visible(false)
                }
                is Resource.Failure -> {
                    isLoading = false
                    this@HomeFragment.handleAPIError(posts) {
                        lifecycleScope.launch {
                            loadMoreItems(
                                isFirstPage
                            )
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
        if (dialog.isShowing) {
            dialog.cancel()
        }
    }
}