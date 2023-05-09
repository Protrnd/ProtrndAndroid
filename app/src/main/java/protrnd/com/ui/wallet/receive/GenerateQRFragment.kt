package protrnd.com.ui.wallet.receive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentGenerateQrBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.setGradient
import protrnd.com.ui.viewmodels.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.*

class GenerateQRFragment :
    BaseFragment<PaymentViewModel, FragmentGenerateQrBinding, PaymentRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = "@${currentUserProfile.username}"
        binding.qrCodeProfileFullName.text = currentUserProfile.fullname
        binding.profileId.text = username
        binding.receiverIdCopy.text = username
        binding.receiverIdCopy.setGradient()
        binding.location.text = currentUserProfile.location

        if (currentUserProfile.acctype == getString(R.string.business))
            binding.qrCodeProfileFullName.setCompoundDrawables(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.business_badge_ic),
                null
            )

        generateQR(0)

        binding.receiveAmount.transformationMethod = null
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var amount: Int
        binding.generateBtn.setOnClickListener {
            imm.hideSoftInputFromWindow(binding.receiveAmount.windowToken, 0)
            binding.receiveAmount.clearFocus()
            amount = binding.receiveAmount.text.toString().toInt()
            if (binding.receiveAmount.text.isEmpty()) {
                generateQR(0)
            } else {
                if (amount < 50) {
                    binding.receiveAmount.error = "Please input an amount 50 and above"
                } else if (currentUserProfile.acctype == getString(R.string.personal) && amount > 1_000_000) {
                    binding.receiveAmount.error =
                        "Only business accounts are allowed to receive above 1,000,000"
                } else
                    generateQR(amount)
            }
        }
    }

    fun generateQR(amount: Int) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val profileQrCodeContent =
            QrCodeContent(profile = currentUserProfile, time = sdf.format(Date()), amount = amount)
        val qrCodeContent = Gson().toJson(profileQrCodeContent)
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(qrCodeContent, BarcodeFormat.QR_CODE, 400, 400)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            Glide.with(requireView())
                .load(bmp)
                .into(binding.qrCode)
        } catch (e: WriterException) {
            Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGenerateQrBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }

}