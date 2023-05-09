package protrnd.com.ui.wallet.send

import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import com.budiyev.android.codescanner.*
import com.google.gson.Gson
import org.jetbrains.anko.runOnUiThread
import protrnd.com.R
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentQrScannerBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.PaymentViewModel

class QRScannerFragment :
    BaseFragment<PaymentViewModel, FragmentQrScannerBinding, PaymentRepository>() {

    companion object {
        const val CAMERA_REQUEST_CODE = 732198
    }

    private lateinit var codeScanner: CodeScanner
    private val scanResultLive = MutableLiveData<QrCodeContent>()
    private val liveData: LiveData<QrCodeContent> = scanResultLive

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment

        codeScanner = CodeScanner(requireContext(), binding.scannerView)
        setupPermission()
        val amount = requireArguments().getString("amount")!!
        val notification = RingtoneManager.getDefaultUri(TYPE_NOTIFICATION)
        val mp = RingtoneManager.getRingtone(requireContext(), notification)

        liveData.observe(viewLifecycleOwner) { content ->
            if (content.amount == 0) {
                if (amount == "" || amount == "0") {
                    Toast.makeText(
                        requireContext(),
                        "Cannot send ₦0, please input a higher value",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    mp.play()
                    content.amount = amount.toInt()
                    val bundle = Bundle()
                    bundle.putParcelable("content", content)
                    hostFragment.navController.navigate(R.id.profileResultFragment, bundle)
                }
            } else {
                mp.play()
                val bundle = Bundle()
                bundle.putParcelable("content", content)
                hostFragment.navController.navigate(R.id.profileResultFragment, bundle)
            }
        }
    }

    private fun setupPermission() {
        val permission =
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
        if (permission == PackageManager.PERMISSION_GRANTED) {
            applyCodeScanner()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        requireContext(),
                        "Please enable camera permission",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    applyCodeScanner()
                }
            }
        }
    }

    fun applyCodeScanner() {
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                try {
                    scanResultLive.postValue(Gson().fromJson(it.text, QrCodeContent::class.java))
                } catch (e: Exception) {
                    requireContext().runOnUiThread {
                        Toast.makeText(
                            this,
                            "Please ensure you are scanning a protrnd QR code",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            errorCallback = ErrorCallback {
                requireContext().runOnUiThread {
                    Toast.makeText(this, "Camera initialization error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentQrScannerBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java)
        val db = ProtrndAPIDataSource().provideTransactionDatabase(requireActivity().application)
        return PaymentRepository(paymentApi)
    }
}