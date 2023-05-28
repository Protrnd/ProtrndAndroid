package protrnd.com.ui.wallet.send

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Profile
import protrnd.com.data.models.QrCodeContent
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PaymentApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.PaymentRepository
import protrnd.com.databinding.FragmentEnterProfileIDBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ProfileTagAdapter
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewholder.ProfileTagViewHolder
import protrnd.com.ui.viewmodels.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.*

class EnterProfileIDFragment :
    BaseFragment<PaymentViewModel, FragmentEnterProfileIDBinding, PaymentRepository>() {

    private var profileResult: Profile = Profile()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHost = parentFragment as NavHostFragment
        val sendMoneyBottomSheetFragment = navHost.parentFragment as SendMoneyBottomSheetFragment

        if (sendMoneyBottomSheetFragment.profile != Profile()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
            val bundle = Bundle()
            bundle.putParcelable(
                "content",
                QrCodeContent(
                    requireArguments().getString("amount").toString().toInt(),
                    sendMoneyBottomSheetFragment.profile,
                    sdf.format(Date()),
                    isInDebugMode = isInDebugMode()
                )
            )
            bundle.putBoolean("isChat", true)
            navHost.navController.navigate(R.id.profileResultFragment, bundle)
        }


        binding.continueBtn.enable(false)
        val sendAmount = "Send ₦${
            requireArguments().getString("amount").toString().trim().toInt().formatAmount()
        }"

        binding.continueBtn.text = sendAmount
        binding.prodileIdInput.addTextChangedListener {
            binding.continueBtn.enable(it.toString().isNotEmpty())
        }

        binding.profileSearchRv.visible(false)

        binding.profileSearchRv.layoutManager = LinearLayoutManager(requireContext())
        var tagsAdapter = ProfileTagAdapter(arrayListOf())
        binding.profileSearchRv.adapter = tagsAdapter

        val profileSearchedMutable = MutableLiveData<Profile>()
        val profileLive: LiveData<Profile> = profileSearchedMutable

        val profilesSearchedMutable = MutableLiveData<List<Profile>>()
        val profilesLive: LiveData<List<Profile>> = profilesSearchedMutable

        profileLive.observe(viewLifecycleOwner) { profile ->
            profileResult = profile
            binding.prodileIdInput.setText(profile.username)
            binding.prodileIdInput.setSelection(profile.username.length)
            profilesSearchedMutable.postValue(listOf())
        }

        profilesLive.observe(viewLifecycleOwner) { result ->
            tagsAdapter = ProfileTagAdapter(result)
            binding.profileSearchRv.adapter = tagsAdapter
            tagsAdapter.clickPosition(object : ProfileClickListener {
                override fun profileClick(
                    holder: ProfileTagViewHolder?,
                    position: Int,
                    profile: Profile
                ) {
                    profileSearchedMutable.postValue(profile)
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.prodileIdInput.windowToken, 0)
                    binding.profileSearchRv.visible(false)
                }
            })
        }

        viewModel.profiles.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    profilesSearchedMutable.postValue(it.value.data)
                }
                is Resource.Loading -> {
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error getting profiles", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {}
            }
        }

        binding.prodileIdInput.addTextChangedListener {
            if (it != null && it.isNotEmpty()) {
                viewModel.searchProfilesByName(it.toString())
                binding.profileSearchRv.visible(true)
            }
        }

        val bundle = Bundle()
        binding.continueBtn.setOnClickListener {
            if (profileResult == Profile()) {
                requireView().errorSnackBar("Please select a profile")
            } else {
                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                bundle.putParcelable(
                    "content",
                    QrCodeContent(
                        requireArguments().getString("amount").toString().toInt(),
                        profileResult,
                        sdf.format(Date()),
                        isInDebugMode = isInDebugMode()
                    )
                )
                navHost.navController.navigate(R.id.profileResultFragment, bundle)
            }
        }
    }

    override fun getViewModel() = PaymentViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEnterProfileIDBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): PaymentRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val paymentApi = ProtrndAPIDataSource().buildAPI(PaymentApi::class.java, token)
        return PaymentRepository(paymentApi)
    }
}