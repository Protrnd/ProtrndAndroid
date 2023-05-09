package protrnd.com.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.R
import protrnd.com.data.models.Chat
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.ChatRepository
import protrnd.com.data.responses.BooleanResponseBody
import protrnd.com.data.responses.ChatConversationResponseBody
import protrnd.com.databinding.ActivityChatContentBinding
import protrnd.com.ui.*
import protrnd.com.ui.adapter.ChatMessagesAdapter
import protrnd.com.ui.adapter.listener.ChatRecyclerViewListener
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.viewholder.ChatMessagesViewHolder
import protrnd.com.ui.viewmodels.ChatViewModel
import protrnd.com.ui.wallet.send.SendMoneyBottomSheetFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatContentActivity :
    BaseActivity<ActivityChatContentBinding, ChatViewModel, ChatRepository>() {
    private lateinit var receiverProfile: Profile
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private var chatContentList = arrayListOf<Chat>()
    private val profileLiveData = MutableLiveData<Profile>()
    private val profileLive: LiveData<Profile> = profileLiveData

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityChatContentBinding.inflate(inflater)

    override fun getViewModel() = ChatViewModel::class.java

    override fun getActivityRepository(): ChatRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ChatApi::class.java, token)
        return ChatRepository(api)
    }

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        setSupportActionBar(binding.chatToolbar)
        val actionBar = supportActionBar!!
        actionBar.title = "Protrnd"
        actionBar.setDisplayHomeAsUpEnabled(true)
        binding.chatToolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)

        intent!!

        val profileid = intent.getStringExtra("profileid")!!

        val profile = MemoryCache.profiles[profileid]
        if (profile != null) {
            val result: Profile = profile
            profileLiveData.postValue(result)
        }

        profileLive.observe(this) { profileResponse ->
            actionBar.title = profileResponse.fullname
            actionBar.subtitle = "@${profileResponse.username}"
            receiverProfile = profileResponse
        }

        viewModel.getProfile(profileid)
        viewModel._receiverProfile.observe(this) {
            when (it) {
                is Resource.Success -> {
                    val response = it.value
                    if (response.successful) {
                        profileLiveData.postValue(response.data)
                        MemoryCache.profiles[profileid] = response.data
                    }
                }
                else -> {}
            }
        }

        binding.sendChatBtn.enable(false)

        var chatMessage = ""
        binding.chatMessageField.addTextChangedListener {
            chatMessage = it.toString().trim()
            binding.sendChatBtn.enable(chatMessage.isNotEmpty())
        }

        binding.sendChatBtn.setOnClickListener {
            val dto = ChatDTO(
                itemid = profileid,
                message = chatMessage,
                receiverid = profileid,
                type = getString(R.string.chat)
            )

            viewModel.sendChat(dto).enqueue(object : Callback<BooleanResponseBody> {
                override fun onResponse(
                    call: Call<BooleanResponseBody>,
                    response: Response<BooleanResponseBody>
                ) {
                    populateConversations(profileid)
                }

                override fun onFailure(call: Call<BooleanResponseBody>, t: Throwable) {
                    binding.root.errorSnackBar("Please connect to the internet!")
                }
            })

            binding.chatMessageField.text.clear()
            binding.sendChatBtn.enable(false)
        }

        chatMessagesAdapter = ChatMessagesAdapter(currentUserProfile, chatContentList, viewModel)
        binding.chatMessagesRv.adapter = chatMessagesAdapter
        val recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                populateConversations(profileid)
            }
        }

        binding.chatMessagesRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerViewReadyCallback.onLayoutReady()
                binding.chatMessagesRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        binding.sendFundBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val sendFund = SendMoneyBottomSheetFragment(profile = receiverProfile, activity = this)
            sendFund.show(supportFragmentManager, sendFund.tag)
        }
    }

    fun populateConversations(profileid: String, isPayment: Boolean = false) {
        val messagesFrom = MemoryCache.chats[profileid]

        val chatMessagesLiveData = MutableLiveData<MutableList<Chat>>()
        val chatMessagesData: LiveData<MutableList<Chat>> = chatMessagesLiveData

        if (messagesFrom != null) {
            val messages: MutableList<Chat> = messagesFrom
            chatMessagesLiveData.postValue(messages)
        }

        chatMessagesData.observe(this) { messagesList ->
            val messages = messagesList.reversed()
            if (!isPayment) {
                if (chatMessagesAdapter.chatList.isEmpty())
                    chatMessagesAdapter.setData(ArrayList(messages))
                else
                    chatMessagesAdapter.setNewData(ArrayList(messages))
            } else {
                chatMessagesAdapter.chatList = ArrayList(messages)
                chatMessagesAdapter.notifyItemInserted(messages.size)
                chatMessagesAdapter.notifyItemChanged(messages.size)
            }
            chatMessagesAdapter.loadChat(object : ChatRecyclerViewListener {
                override fun loadChat(holder: ChatMessagesViewHolder, chatMessage: Chat) {
                    holder.bind(
                        chatMessage,
                        currentUserProfile,
                        viewModel,
                        this@ChatContentActivity
                    )
                    holder.view.postImageReceived.setOnClickListener {
                        startActivity(
                            Intent(
                                this@ChatContentActivity,
                                PostActivity::class.java
                            ).apply {
                                putExtra("post_id", chatMessage.itemid)
                            })
                        startAnimation()
                    }
                    holder.view.sentPostImage.setOnClickListener {
                        startActivity(
                            Intent(
                                this@ChatContentActivity,
                                PostActivity::class.java
                            ).apply {
                                putExtra("post_id", chatMessage.itemid)
                            })
                        startAnimation()
                    }
                }
            })
            binding.chatMessagesRv.scrollToPosition(messages.size - 1)
        }

        viewModel.getChatConversationData(profileid)
            .enqueue(object : Callback<ChatConversationResponseBody> {
                override fun onResponse(
                    call: Call<ChatConversationResponseBody>,
                    response: Response<ChatConversationResponseBody>
                ) {
                    val body = response.body()
                    if (body != null) {
                        val data = body.data.toMutableList()
                        if (messagesFrom == null) {
                            chatMessagesLiveData.postValue(data)
                        } else {
                            val messages = MemoryCache.chats[profileid]
                            if (messages != null) {
                                val m: MutableList<Chat> = messages
                                chatMessagesLiveData.postValue(m)
                            }
                        }
                        MemoryCache.chats[profileid] = data
                    }
                }

                override fun onFailure(call: Call<ChatConversationResponseBody>, t: Throwable) {
                    val messages = MemoryCache.chats[profileid]
                    if (messages != null) {
                        val m: MutableList<Chat> = messages
                        chatMessagesLiveData.postValue(m)
                    }
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishActivity()
        }
        return true
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }

}