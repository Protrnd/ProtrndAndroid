package protrnd.com.ui.post

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import protrnd.com.R
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.ChatRepository
import protrnd.com.data.responses.BooleanResponseBody
import protrnd.com.databinding.ForwardPostLayoutBinding
import protrnd.com.ui.HashTagResultsActivity
import protrnd.com.ui.adapter.ProfileTagAdapter
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.base.ViewModelFactory
import protrnd.com.ui.enable
import protrnd.com.ui.home.HomeFragment
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.profile.ProfileFragment
import protrnd.com.ui.search.SearchFragment
import protrnd.com.ui.viewholder.ProfileTagViewHolder
import protrnd.com.ui.viewmodels.ChatViewModel
import protrnd.com.ui.visible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForwardPostBottomSheetDialog(
    val currentProfile: Profile,
    val post: Post,
    val token: String,
    val fragment: Fragment? = null,
    val activity: Activity? = null
) : BottomSheetDialogFragment() {

    lateinit var chatViewModel: ChatViewModel
    lateinit var forwardPostLayoutBinding: ForwardPostLayoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val a = it as BottomSheetDialog
            a.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sent = "Sent"

        val profileSearchLive = MutableLiveData<List<Profile>>()
        val live: LiveData<List<Profile>> = profileSearchLive
        val sentTo = arrayListOf<Profile>()
        forwardPostLayoutBinding.profilesResultRv.layoutManager =
            LinearLayoutManager(requireContext())

        live.observe(viewLifecycleOwner) {
            val list = it.toMutableList()
            list.remove(currentProfile)
            val adapter = ProfileTagAdapter(list.toList(), true)
            forwardPostLayoutBinding.profilesResultRv.adapter = adapter
            adapter.clickPosition(object : ProfileClickListener {
                override fun profileClick(
                    holder: ProfileTagViewHolder?,
                    position: Int,
                    profile: Profile
                ) {
                    sentTo.add(profile)
                    if (holder != null) {
                        holder.view.sendBtn.text = sent
                        holder.view.sendBtn.enable(false)
                    }
                    chatViewModel.sendChat(
                        ChatDTO(
                            itemid = post.id,
                            message = forwardPostLayoutBinding.messageText.text.toString().trim(),
                            profile.id,
                            "forward"
                        )
                    ).enqueue(object : Callback<BooleanResponseBody> {
                        override fun onResponse(
                            call: Call<BooleanResponseBody>,
                            response: Response<BooleanResponseBody>
                        ) {
                            if (response.isSuccessful && response.body()!!.data) {
                                Toast.makeText(requireContext(), sent, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<BooleanResponseBody>, t: Throwable) {
                        }
                    })
                }
            })
        }

        chatViewModel._searchProfile.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    profileSearchLive.postValue(it.value.data)
                    forwardPostLayoutBinding.profileResultEmpty.visible(it.value.data.isEmpty())
                }
                is Resource.Loading -> {
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error getting profiles", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        forwardPostLayoutBinding.searchUserProfileText.addTextChangedListener {
            if (it != null && it.isNotEmpty()) {
                chatViewModel.searchProfilesByName(it.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        forwardPostLayoutBinding = ForwardPostLayoutBinding.inflate(layoutInflater)
        val datasource = ProtrndAPIDataSource()
        val api = datasource.buildAPI(ChatApi::class.java, token)
        val sr = ChatRepository(api)
        val factory = ViewModelFactory(sr)
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
        return forwardPostLayoutBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragment is SearchFragment)
            fragment.removeAlphaVisibility()
        if (fragment is HomeFragment)
            fragment.removeAlphaVisibility()
        if (activity is PostActivity)
            activity.removeAlphaVisibility()
        if (activity is HashTagResultsActivity)
            activity.removeAlphaVisibility()
        if (activity is ProfileActivity)
            activity.removeAlphaVisibility()
        if (fragment is ProfileFragment)
            fragment.removeAlphaVisibility()
    }
}