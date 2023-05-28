package protrnd.com.ui.home

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.ProfileDTO
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.backgroundtask.SavePostsService
import protrnd.com.data.network.backgroundtask.SaveProfileService
import protrnd.com.data.network.backgroundtask.SaveTransactionsService
import protrnd.com.data.network.backgroundtask.SendMessageRequestService
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.FragmentHomeBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>() {

    private var addButtonClicked: Boolean = false
    private lateinit var adapter: PostsPagingAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private lateinit var dialog: Dialog
    private lateinit var thisActivity: HomeActivity
    private lateinit var dexter: DexterBuilder
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private val rotateOpenAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open
        )
    }
    private val rotateCloseAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close
        )
    }
    private val fromBottomAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom
        )
    }
    private val toBottomAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_bottom
        )
    }

    override fun onStart() {
        super.onStart()
        checkNotifications()
    }

    fun removeLoader() {
        if (binding.root.isRefreshing)
            binding.root.isRefreshing = false
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        super.onViewReady(savedInstanceState)
        thisActivity = activity as HomeActivity
        postsLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.postsRv.layoutManager = postsLayoutManager
        adapter = PostsPagingAdapter()
        binding.postsRv.adapter = adapter

        binding.floatingActionButtonAdd.setOnClickListener {
            onAddButtonClicked()
        }

        binding.floatingActionButtonScan.setOnClickListener {
            onAddButtonClicked()
            binding.alphaBg.visible(true)
            val scanBottomSheetDialog = SendMoneyBottomSheetFragment(this)
            scanBottomSheetDialog.show(childFragmentManager, scanBottomSheetDialog.tag)
        }

        binding.floatingActionButtonMessage.setOnClickListener {
            thisActivity.setChatChecked()
            onAddButtonClicked()
        }

        dialog = Dialog(requireContext())
        val locationPickerBinding = LocationPickerBinding.inflate(layoutInflater)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setContentView(locationPickerBinding.root)

        if (currentUserProfile.location == null || currentUserProfile.location!!.isEmpty()) {
            dialog.show()
        } else {
            NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
                binding.postsRv.loadPageData(
                    childFragmentManager,
                    thisActivity,
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
            }
        }

        binding.floatingActionButtonUpload.setOnClickListener {
            startActivity(Intent(requireContext(), NewPostActivity::class.java))
        }

        binding.root.setOnRefreshListener {
            if (thisActivity.isNetworkAvailable()) {
                adapter.submitData(lifecycle, PagingData.empty())
                requestPageLoaded()
                try {
                    setupData()
                }catch (_: Throwable) {
                    Toast.makeText(requireContext(), "An error occurred!", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.root.errorSnackBar("Please check your network connection")
                binding.root.isRefreshing = false
            }
        }

        var state = "Abia"
        var city = "Aba"

        locationPickerBinding.statePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            city = ""
            locationPickerBinding.cityPicker.clearSelectedItem()
            state = newItem
            locationPickerBinding.selectState(state)
            locationPickerBinding.saveBtn.enable(false)
        }

        locationPickerBinding.cityPicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            city = newItem
            locationPickerBinding.saveBtn.enable(true)
        }

        locationPickerBinding.statePicker.selectItemByIndex(0)
        locationPickerBinding.cityPicker.selectItemByIndex(0)

        locationPickerBinding.saveBtn.setOnClickListener {
            currentUserProfile.location = "$state,$city"
            updateProfile("$state,$city")
            dialog.dismiss()
            binding.postsRv.loadPageData(
                childFragmentManager,
                thisActivity,
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
        }

        NetworkConnectionLiveData(requireContext()).observe(viewLifecycleOwner) {
            requestPosts()
        }
    }

    private fun updateProfile(location: String) {
        val profile = currentUserProfile
        profile.location = location
        val dto = ProfileDTO(
            profileImage = currentUserProfile.profileimg,
            backgroundImageUrl = currentUserProfile.bgimg,
            accountType = currentUserProfile.acctype,
            location = location,
            email = currentUserProfile.email,
            about = currentUserProfile.about.toString(),
            fullName = currentUserProfile.fullname,
            userName = currentUserProfile.username
        )

        val data = Data.Builder()
            .putString("authToken",token)
            .putString("profile",Gson().toJson(dto))
            .build()

        val worker = OneTimeWorkRequest.Builder(SaveProfileService::class.java)
            .setInputData(data)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(requireContext()).enqueue(worker)

        lifecycleScope.launch {
            profilePreferences.saveProfile(profile)
        }
    }

    private fun loadPage() {
        //Load first page
        requestPosts()
    }

    private fun requestPosts() {
        viewModel.getPostByPage().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    adapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {
                            binding.root.isRefreshing = true
                        } else {
                            binding.root.isRefreshing = false
                            if (adapter.itemCount < 1) {
                                binding.root.errorSnackBar("Error loading posts") { loadPage() }
                            }
                        }
                    }
                }
            }
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter.snapshot().isEmpty()) {
            setupData()
        }
    }

    private fun setupData() {
        loadPage()
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
        val postDatabase = protrndAPIDataSource.providePostDatabase(requireActivity().application)
        val profileDatabase =
            protrndAPIDataSource.provideProfileDatabase(requireActivity().application)
        return HomeRepository(api, postsApi, postDatabase, profileDatabase)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog.isShowing) {
            dialog.cancel()
        }
    }

    private fun requestPageLoaded() {
        binding.postsRv.loadPageData(
            childFragmentManager,
            thisActivity,
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
    }

    private fun onAddButtonClicked() {
        if (!addButtonClicked) {
            binding.alphaBg.visible(true)
            binding.floatingActionButtonScan.visible(true)
            binding.floatingActionButtonUpload.visible(true)
            binding.floatingActionButtonMessage.visible(true)
            setAnimation(true)
        } else {
            binding.alphaBg.visible(false)
            binding.floatingActionButtonScan.visible(false)
            binding.floatingActionButtonUpload.visible(false)
            binding.floatingActionButtonMessage.visible(false)
            setAnimation(false)
        }
    }

    private fun setAnimation(buttonClicked: Boolean) {
        if (buttonClicked) {
            binding.floatingActionButtonScan.startAnimation(fromBottomAnimation)
            binding.floatingActionButtonUpload.startAnimation(fromBottomAnimation)
            binding.floatingActionButtonMessage.startAnimation(fromBottomAnimation)
            binding.floatingActionButtonAdd.startAnimation(rotateOpenAnimation)
        } else {
            binding.floatingActionButtonScan.startAnimation(toBottomAnimation)
            binding.floatingActionButtonUpload.startAnimation(toBottomAnimation)
            binding.floatingActionButtonMessage.startAnimation(toBottomAnimation)
            binding.floatingActionButtonAdd.startAnimation(rotateCloseAnimation)
        }
        addButtonClicked = !addButtonClicked
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

    fun showAlpha() {
        binding.alphaBg.visible(true)
    }

    private fun checkNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dexter = Dexter.withContext(requireContext())
                .withPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ).withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    }

                    override fun onPermissionDenied(report: PermissionDeniedResponse?) {
                        report.let {
                            if (report != null) {
                                if (!report.isPermanentlyDenied) {
                                    val requestNotificationIntent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            val uri = Uri.fromParts(
                                                "package",
                                                requireActivity().packageName,
                                                null
                                            )
                                            data = uri
                                        }
                                    resultLauncher.launch(requestNotificationIntent)
                                }
                            }
                        }

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        request: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).withErrorListener {
                    Toast.makeText(requireContext(), "An Error occurred!", Toast.LENGTH_SHORT)
                        .show()
                }
            dexter.onSameThread().check()
        }
    }

    override fun onPause() {
        super.onPause()
        if (addButtonClicked)
            onAddButtonClicked()
    }

    override fun onDetach() {
        super.onDetach()
        if (addButtonClicked)
            onAddButtonClicked()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (addButtonClicked)
            onAddButtonClicked()
    }

    override fun onStop() {
        super.onStop()
        if (addButtonClicked)
            onAddButtonClicked()
    }
}