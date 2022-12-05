package protrnd.com.ui.promotion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.network.PostApi
import protrnd.com.data.network.ProfileApi
import protrnd.com.data.network.Resource
import protrnd.com.data.repository.BaseRepository
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityNewPromotionBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.getSerializable
import protrnd.com.ui.handleAPIError
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.setupHomeIndicator
import protrnd.com.ui.visible
import java.io.File
import java.util.*

class NewPromotionActivity : BaseActivity<ActivityNewPromotionBinding, HomeViewModel, BaseRepository>() {
    private lateinit var bannerUri: Uri

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        this.setupHomeIndicator(binding.promotionsTb)
        val post = intent!!.getSerializable("post_details", Post::class.java)
        viewModel.getCurrentProfile()

        viewModel.profile.observe(this){
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    binding.resultsView.visible(true)

//                    binding.bindPostDetails(
//                        usernameTv = binding.promotionsUsername,
//                        fullnameTv = binding.
//                        captionTv = binding.promotionsCaptionTv,
//                        locationTv = binding.promotionsLocation,
//                        imagesPager = binding.promotionsImagesViewPager,
//                        postOwnerProfile = it.value.data,
//                        post = post,
//                        profileImage = binding.promotionsPostOwnerImage,
//                        tabLayout = binding.tabLayout
//                    )
                }
                is Resource.Loading -> {
                    binding.progressBar.visible(true)
                    binding.resultsView.visible(false)
                }
                is Resource.Failure -> {
                    handleAPIError(binding.root, it)
                    onDestroy()
                }
            }
        }

        binding.bannerImage.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater)= ActivityNewPromotionBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository() : HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}