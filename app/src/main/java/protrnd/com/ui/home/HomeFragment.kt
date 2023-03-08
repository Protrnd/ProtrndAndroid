package protrnd.com.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Location
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
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
import protrnd.com.ui.viewholder.PostsViewHolder

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>() {

    private lateinit var adapter: PostsPagingAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private lateinit var dialog: Dialog
    private val locationHash = HashMap<String, List<String>>()
    private lateinit var thisActivity: HomeActivity
    private lateinit var recyclerViewReadyCallback: RecyclerViewReadyCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisActivity = activity as HomeActivity
        postsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postsRv.layoutManager = postsLayoutManager
        if (thisActivity.lmState != null) {
            binding.postsRv.layoutManager!!.onRestoreInstanceState(thisActivity.lmState)
            thisActivity.lmState = null
        }

        binding.postsRv.apply {
            adapter = PostsPagingAdapter()
            this@HomeFragment.adapter = adapter as PostsPagingAdapter
        }

        recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                if (!snapshotExists())
                    setupRecyclerView()
            }
        }

        binding.postsRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerViewReadyCallback.onLayoutReady()
                binding.postsRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        if (requireActivity().isNetworkAvailable()) {
            setupData()
        } else {
            getStoredDataFromLocalDB()
        }

        binding.root.setOnRefreshListener {
            if (thisActivity.isNetworkAvailable()) {
                adapter.submitData(lifecycle, PagingData.empty())
                requestPosts()
            } else {
                binding.root.snackbar("Please check your network connection")
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (binding.root.isRefreshing) {
                    binding.root.isRefreshing = false
                }
            }, 3000)
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
                updateProfile()
                dialog.dismiss()
            }
        }
    }

    private fun updateProfile() {
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
        lifecycleScope.launch {
            settingsPreferences.saveProfile(thisActivity.currentUserProfile)
        }
    }

    private fun loadPage() {
        if (thisActivity.currentUserProfile.location.toString().isEmpty())
            loadLocations()
        //Load first page
        lifecycleScope.launch { requestPosts() }
    }

    private fun requestPosts() {
        viewModel.getPostByPage().observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun loadLocations() {
        viewModel.getLocations()
    }

    private fun setupRecyclerView() {
        adapter.setupRecyclerResults(object : PostsPagingAdapter.SetupRecyclerResultsListener {
            override fun setupLikes(holder: PostsViewHolder, postData: Post) {
                NetworkConnectionLiveData(context ?: return)
                    .observe(viewLifecycleOwner) { isConnected ->
                        if (isConnected) {
                            lifecycleScope.launch {
                                setupPostLikes(holder, postData)
                            }
                        }
                    }
            }

            override fun setupData(holder: PostsViewHolder, postData: Post) {
                NetworkConnectionLiveData(context ?: return)
                    .observe(viewLifecycleOwner) {
                        lifecycleScope.launch {
                            setupPosts(holder, postData)
                        }
                        binding.shimmerLayout.stopShimmerAnimation()
                        binding.shimmerLayout.visible(false)
                        binding.postsRv.visible(true)
                    }
            }

            override fun showCommentSection(postData: Post) {
                lifecycleScope.launch {
                    val profileResult = getOtherProfile(postData.profileid)
                    if (profileResult != null) {
                        requireContext().showCommentSection(
                            viewModel,
                            viewLifecycleOwner,
                            lifecycleScope,
                            profileResult,
                            thisActivity.currentUserProfile,
                            postData.identifier
                        )
                    }
                }
            }

            override fun like(holder: PostsViewHolder, postData: Post) {
                lifecycleScope.launch {
                    likePost(holder, postData)
                }
            }
        })
    }

    private suspend fun likePost(holder: PostsViewHolder, postData: Post) {
        val profileResult = getOtherProfile(postData.profileid)
        if (profileResult != null) {
            val liked = holder.view.likeToggle.isChecked
            if (requireActivity().isNetworkAvailable())
                likePost(
                    holder.view.likeToggle,
                    holder.view.likesCount,
                    lifecycleScope,
                    viewModel,
                    postData.identifier,
                    profileResult,
                    thisActivity.currentUserProfile
                )
            else
                holder.view.likeToggle.isChecked = !liked
        }
    }

    private fun setupData() {
        loadPage()
    }

    private fun getStoredDataFromLocalDB() {
        viewModel.getSavedPosts()?.asLiveData()?.observe(viewLifecycleOwner) { saved ->
            if (saved.isNotEmpty()) {
                adapter.submitData(viewLifecycleOwner.lifecycle, PagingData.from(saved))
            }
        }
    }

    override fun onPause() {
        savePosts()
        super.onPause()
    }

    private fun savePosts() {
        lifecycleScope.launch {
            val postItems = adapter.snapshot().items
            if (postItems.isNotEmpty())
                viewModel.savePosts(postItems)
        }
        thisActivity.lmState = postsLayoutManager.onSaveInstanceState()!!
    }

    private fun snapshotExists(): Boolean {
        return adapter.snapshot().items.isNotEmpty()
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

    private suspend fun getOtherProfile(
        id: String
    ): Profile? {
        return getStoredProfile(id) ?: when (val otherProfile = viewModel.getProfileById(id)) {
            is Resource.Success -> {
                viewModel.saveProfile(otherProfile.value.data)
                return otherProfile.value.data
            }
            is Resource.Loading -> {
                return null
            }
            is Resource.Failure -> {
                return null
            }
            else -> {
                return null
            }
        }
    }

    private fun getStoredProfile(id: String): Profile? {
        val otherProfile = viewModel.getProfile(id)
        var result: Profile? = null
        otherProfile?.asLiveData()?.observe(viewLifecycleOwner) {
            if (it != null) {
                result = it
            }
        }
        return result
    }

    suspend fun setupPostLikes(holder: PostsViewHolder, postData: Post) {
        viewModel.setupLikes(
            postData.id,
            holder.view.likesCount,
            holder.view.likeToggle
        )
    }

    suspend fun setupPosts(holder: PostsViewHolder, postData: Post) {
        val profileResult = getOtherProfile(postData.profileid)
        if (profileResult != null) {
            holder.bind(
                requireActivity(),
                postData,
                profileResult,
                thisActivity.currentUserProfile
            )
        }
    }
}