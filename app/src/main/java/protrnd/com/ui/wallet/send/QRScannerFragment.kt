package protrnd.com.ui.wallet.send

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import org.jetbrains.anko.runOnUiThread
import protrnd.com.R
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentQrScannerBinding
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.payment.PaymentViewModel

class QRScannerFragment : BaseFragment<PaymentViewModel, FragmentQrScannerBinding, PaymentRepository>() {

    companion object {
        const val CAMERA_REQUEST_CODE = 732198
    }

    private var codeScanner: CodeScanner? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostFragment = parentFragment as NavHostFragment
        codeScanner = CodeScanner(requireContext(),binding.scannerView)
        val amount = requireArguments().getString("amount").toString()
        val bundle = Bundle()
        bundle.putString("amount",amount)

        codeScanner!!.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                requireContext().runOnUiThread {
                    hostFragment.navController.navigate(R.id.profileResultFragment,bundle)
                }
            }

            errorCallback = ErrorCallback {
                requireContext().runOnUiThread {
                    Toast.makeText(requireContext(), "Camera initialization error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        setupPermission()
    }

    private fun setupPermission() {
        val permission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
        if (permission == PackageManager.PERMISSION_GRANTED) {
            codeScanner!!.startPreview()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf( android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Please enable camera permission", Toast.LENGTH_SHORT).show()
                } else {
                    codeScanner!!.startPreview()
                }
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentQrScannerBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = PaymentRepository()
}