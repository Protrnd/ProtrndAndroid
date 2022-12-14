package protrnd.com.ui.promotion

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Location
import protrnd.com.data.models.Post
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.BaseRepository
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityNewPromotionBinding
import protrnd.com.databinding.LocationPickerBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.SelectedLocationAdapter
import protrnd.com.ui.adapter.listener.PositionClickListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.checkout.CheckoutActivity
import protrnd.com.ui.home.HomeViewModel
import java.io.File
import java.util.*

class NewPromotionActivity :
    BaseActivity<ActivityNewPromotionBinding, HomeViewModel, BaseRepository>() {
    private lateinit var bannerUri: Uri
    private val locationHash = HashMap<String, List<String>>()
    private val selectedLocations = ArrayList<String>()
    private val statesWithNoCity: MutableSet<String> = ArraySet()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val outputUri = File(filesDir, "${Date().time}.jpg").toUri()
                val listUri = listOf(uri, outputUri)
                cropImage.launch(listUri)
            }
        }

    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri)
                .withAspectRatio(16f, 9f)
                .withMaxResultSize(1920, 1080)

            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return UCrop.getOutput(intent!!)!!
        }
    }

    private val cropImage = registerForActivityResult(uCropContract) { uri ->
        binding.selectToAddTv.visible(false)
        bannerUri = uri
        Glide.with(this).load(uri).into(binding.bannerImage)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        this.setupHomeIndicator(binding.promotionsTb)
        val post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent!!.getParcelableExtra("post_details", Post::class.java)
        } else {
            intent!!.getParcelableExtra("post_details")
        }!!
        binding.locationRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = SelectedLocationAdapter(selectedLocations)
        binding.locationRv.adapter = adapter
        binding.resultsView.visible(true)
        binding.bindPostDetails(
            usernameTv = binding.promotionsUsername,
            fullnameTv = binding.fullname,
            captionTv = binding.promotionsCaptionTv,
            locationTv = binding.promotionsLocation,
            imagesPager = binding.promotionsImagesViewPager,
            postOwnerProfile = currentUserProfile,
            post = post,
            profileImage = binding.promotionsPostOwnerImage,
            tabLayout = binding.tabLayout,
            timeText = binding.promotionTimeUploaded
        )
        binding.bannerImage.setOnClickListener {
            getContent.launch("image/*")
        }

        val dialog = Dialog(this)
        val locationPickerBinding = LocationPickerBinding.inflate(layoutInflater)
        dialog.setContentView(locationPickerBinding.root)
        dialog.setCanceledOnTouchOutside(false)
        binding.addLocationBtn.setOnClickListener {
            viewModel.getLocations()
            dialog.show()
        }
        viewModel.locations.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val locations: List<Location> = resource.value.data
                    for (location in locations)
                        locationHash[location.state] = location.cities
                    val states = ArrayList<String>()
                    for (state in locationHash.keys)
                        states.add(state)
                    locationPickerBinding.statePicker.setItems(states)
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
                    handleAPIError(
                        binding.root,
                        resource
                    ) { lifecycleScope.launch { loadLocations() } }
                }
            }
        }

        binding.chargesGroup.setOnCheckedChangeListener { _, checkedId ->
            var total = 0
            when (checkedId) {
                R.id.daily_btn -> {
                    for (location in selectedLocations) {
                        total += if (location.contains("/")) {
                            1000
                        } else {
                            2500
                        }
                    }
                }
                R.id.weekly_btn -> {
                    for (location in selectedLocations) {
                        total += if (location.contains("/")) {
                            3000
                        } else {
                            5000
                        }
                    }
                }
                R.id.monthly_btn -> {
                    for (location in selectedLocations) {
                        total += if (location.contains("/")) {
                            10000
                        } else {
                            15000
                        }
                    }
                }
            }
            val payable = "Total amount payable: ₦ $total"
            binding.payableTv.text = payable
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
            if (state.isEmpty() && city.isEmpty())
                Toast.makeText(
                    this,
                    "Please select a state or city",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                if (state.isNotEmpty() && city.isEmpty() && !selectedLocations.contains(state)) {
                    statesWithNoCity.add(state)
                    selectedLocations.removeAll { l -> l.contains(state) }
                    selectedLocations.add(state)
                    adapter.notifyDataSetChanged()
                } else if (state.isNotEmpty() && city.isNotEmpty()) {
                    if (!statesWithNoCity.contains(state)) {
                        selectedLocations.add("$state/$city")
                        adapter.notifyItemInserted(selectedLocations.size - 1)
                        adapter.notifyItemRangeChanged(0, selectedLocations.size - 1)
                    } else {
                        Toast.makeText(
                            this,
                            "You have selected this state already",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                setPayableTotal()
                dialog.dismiss()
            }
        }

        adapter.positionClick(object : PositionClickListener {
            override fun positionClick(position: Int) {
                if (!selectedLocations[position].contains("/"))
                    statesWithNoCity.remove(selectedLocations[position])
                selectedLocations.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, selectedLocations.size)
                setPayableTotal()
            }
        })

        binding.proceedBtn.setOnClickListener {
            val totalPayable =
                binding.payableTv.text.toString().removePrefix("Total amount payable: ₦ ").toInt()
            if (selectedLocations.isNotEmpty() && bannerUri != Uri.EMPTY && totalPayable > 0) {
                Intent(this, CheckoutActivity::class.java).apply {
                    putExtra("amount", totalPayable)
                    startActivity(this)
                }
            }
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityNewPromotionBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadLocations() {
        viewModel.getLocations()
    }

    fun setPayableTotal() {
        var total = 0
        for (location in selectedLocations) {
            if (location.contains("/")) {
                if (binding.dailyBtn.isChecked)
                    total += 1000
                if (binding.weeklyBtn.isChecked)
                    total += 3000
                if (binding.monthlyBtn.isChecked)
                    total += 10000
            } else {
                if (binding.dailyBtn.isChecked)
                    total += 2500
                if (binding.weeklyBtn.isChecked)
                    total += 5000
                if (binding.monthlyBtn.isChecked)
                    total += 15000
            }
        }
        val payable = "Total amount payable: ₦ $total"
        binding.payableTv.text = payable
    }
}