package protrnd.com.ui.promotion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import protrnd.com.R
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentPromotionBannerBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.enable
import protrnd.com.ui.viewmodels.PaymentViewModel
import java.io.File
import java.util.*

class PromotionBannerFragment :
    BaseFragment<PaymentViewModel, FragmentPromotionBannerBinding, PaymentRepository>() {
    private var promotionUri: Uri = Uri.EMPTY

    private fun getContent() {
        val outputUri = File(requireContext().filesDir, "${Date().time}.jpg").toUri()
        val listUri = listOf(promotionUri, outputUri)
        cropImage.launch(listUri)
    }

    private val cropImage = registerForActivityResult(cropImagePicker()) { uri ->
        if (uri != null) {
            promotionUri = uri
            Glide.with(requireContext())
                .load(uri)
                .into(binding.promotionImage)
        }
    }

    private val getImagePickerResult =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                promotionUri = uri
                getContent()
            } else {
                Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val width = binding.bannerSelectorLayout.width
//        val height = (width*9)/16
//        val params = binding.bannerSelectorLayout.layoutParams
//        params.height = height
//        params.width = width
//        binding.bannerSelectorLayout.requestLayout()

        binding.bannerSelectorLayout.setOnClickListener {
            getImagePickerResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val hostFragment = parentFragment as NavHostFragment
        binding.continueBtn.setOnClickListener {
            if (promotionUri != Uri.EMPTY) {
                binding.root.enable(false)
                lifecycleScope.launch {
                    val uploaded = viewModel.uploadImage(promotionUri, currentUserProfile.username)
                    if (uploaded.isNotEmpty()) {
                        requireArguments().putString("bannerurl", uploaded)
                        hostFragment.navController.navigate(
                            R.id.chooseSupportPaymentMethodFragment,
                            requireArguments()
                        )
                    } else {
                        binding.root.enable(true)
                        Toast.makeText(
                            requireContext(),
                            "Error uploading banner",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please select a banner", Toast.LENGTH_SHORT)
                    .show()
                binding.root.enable(true)
            }
        }
    }

    private fun cropImagePicker() =
        object : ActivityResultContract<List<Uri>, Uri>() {
            override fun createIntent(context: Context, input: List<Uri>): Intent {
                val inputUri = input[0]
                val outputUri = input[1]

                val options = UCrop.Options()
                options.setCompressionQuality(50)
                options.setShowCropGrid(true)
                val uCrop = UCrop.of(inputUri, outputUri)
                    .withAspectRatio(16f, 9f)
                    .withOptions(options)

                return uCrop.getIntent(context)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri {
                return try {
                    UCrop.getOutput(intent!!)!!
                } catch (e: Exception) {
                    Uri.EMPTY
                }
            }
        }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPromotionBannerBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}