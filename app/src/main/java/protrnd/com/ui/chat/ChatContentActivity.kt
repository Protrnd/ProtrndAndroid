package protrnd.com.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.Chat
import protrnd.com.data.models.ChatDTO
import protrnd.com.data.models.ConversationId
import protrnd.com.data.models.Profile
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.network.backgroundtask.SaveMessagesService
import protrnd.com.data.network.backgroundtask.SendMessageRequestService
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.ChatRepository
import protrnd.com.data.responses.BasicResponseBody
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
import java.util.*
import kotlin.collections.ArrayList

class ChatContentActivity :
    BaseActivity<ActivityChatContentBinding, ChatViewModel, ChatRepository>() {
    private var receiverProfile: Profile = Profile()
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private var chatContentList = arrayListOf<Chat>()
    private val profileLiveData = MutableLiveData<Profile>()
    private val profileLive: LiveData<Profile> = profileLiveData
    var convoid = ""
    val chatMessagesLiveData = MutableLiveData<MutableList<Chat>>()
    private val chatMessagesData: LiveData<MutableList<Chat>> = chatMessagesLiveData
    val convoIdMutable = MutableLiveData<String>()
    private val convoIdLive: LiveData<String> = convoIdMutable

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityChatContentBinding.inflate(inflater)

    override fun getViewModel() = ChatViewModel::class.java

    override fun getActivityRepository(): ChatRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ChatApi::class.java, token)
        val dao = protrndAPIDataSource.provideChatDatabase(application)
        val idDao = protrndAPIDataSource.provideConversationIdDatabase(application)
        val convoDb = protrndAPIDataSource.provideConversationDatabase(application)
        val profileDb = protrndAPIDataSource.provideProfileDatabase(application)
        return ChatRepository(api, dao, conversationIdDb = idDao, conversationDb = convoDb, profileDb = profileDb)
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
        convoid = intent.getStringExtra("convoid")!!
        val recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                populateConversations(convoid)
            }
        }

        if (convoid.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val id =
                    viewModel.getStoredConversationId(convoid)
                        ?.first()?.convoid ?: ""
                if (id.isNotEmpty()) {
                    convoIdMutable.postValue(id)
                } else {
                    viewModel.getConversationId(profileid).enqueue(object : Callback<BasicResponseBody> {
                        override fun onResponse(
                            call: Call<BasicResponseBody>,
                            response: Response<BasicResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                convoIdMutable.postValue("${response.body()?.data}")
                                viewModel.saveConversationId(ConversationId(profileid, convoid))
                            }
                        }

                        override fun onFailure(call: Call<BasicResponseBody>, t: Throwable) {
                        }
                    })
                }
            }
        } else {
            convoIdMutable.postValue(convoid)
        }

        convoIdLive.observe(this) { convoidLiveId ->
            viewModel.saveConversationId(ConversationId(convoid = convoidLiveId, profileid = profileid))
            convoid = convoidLiveId
        }

        val profileStoredFlow = viewModel.getStoredProfile(profileid)
        CoroutineScope(Dispatchers.IO).launch {
            val profile = profileStoredFlow?.first()
            if (profile != null) {
                val result: Profile = profile
                profileLiveData.postValue(result)
            }
        }

        profileLive.observe(this) { profileResponse ->
            actionBar.title = profileResponse.fullname
            actionBar.subtitle = "@${profileResponse.username}"
            receiverProfile = profileResponse
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.saveProfile(receiverProfile)
            }
        }

        viewModel.getProfile(profileid)
        viewModel._receiverProfile.observe(this) {
            when (it) {
                is Resource.Success -> {
                    val response = it.value
                    if (response.successful) {
                        if (receiverProfile == Profile())
                            profileLiveData.postValue(response.data)
                    }
                }
                else -> {}
            }
        }

        binding.sendChatBtn.enable(false)

        binding.chatMessageField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                binding.chatMessagesRv.scrollToPosition(chatMessagesAdapter.chatList.size - 1)
        }

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
                convoid = convoid,
                type = getString(R.string.chat)
            )
            addNewMessage(dto)
        }

        chatMessagesAdapter = ChatMessagesAdapter(currentUserProfile, chatContentList, viewModel)
        binding.chatMessagesRv.adapter = chatMessagesAdapter

        binding.chatMessagesRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerViewReadyCallback.onLayoutReady()
                binding.chatMessagesRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        binding.sendFundBtn.setOnClickListener {
            binding.alphaBg.visible(true)
            val sendFund = SendMoneyBottomSheetFragment(profile = receiverProfile, activity = this, convoid = convoid)
            sendFund.show(supportFragmentManager, sendFund.tag)
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
    }

    fun addNewMessage(dto: ChatDTO) {
        val chat = Chat(id = UUID.randomUUID().toString().lowercase(), message = dto.message, type = dto.type, receiverid = dto.receiverid, itemid = dto.itemid, time = getDateTimeFormatted(), convoid = this.convoid, senderid = currentUserProfile.id)
        chatMessagesAdapter.sendMessage(chat)
        sendMessage(dto)
        binding.chatMessageField.text.clear()
        binding.sendChatBtn.enable(false)
        binding.chatMessagesRv.scrollToPosition(chatMessagesAdapter.chatList.size - 1)
        saveMessage(arrayListOf(chat))
    }

    var existing = 0

    fun loadInfo() {
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

    }

    fun populateConversations(convoid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val messagesS = viewModel.getChatMessages(convoid)?.first()
            if (messagesS != null) {
                val m: MutableList<Chat> = messagesS.toMutableList()
                existing = m.size
                m.sortBy { i -> i.time }
                chatMessagesAdapter = ChatMessagesAdapter(currentUserProfile, ArrayList(m), viewModel)
                loadInfo()
                withContext(Dispatchers.Main) {
                    binding.chatMessagesRv.adapter = chatMessagesAdapter
                    binding.chatMessagesRv.scrollToPosition(chatMessagesAdapter.chatList.size - 1)
                }
            }
        }

        chatMessagesData.observe(this) { messagesList ->
            val messages = messagesList.reversed()

            if (isNetworkAvailable()) {
                if (chatMessagesAdapter.chatList.isEmpty()) {
                    chatMessagesAdapter.setData(ArrayList(messages))
                }
                else {
                    val original = chatMessagesAdapter.chatList.size
                    val new = messagesList.size
                    if (new > original) {
                        chatMessagesAdapter.setNewData(ArrayList(messages))
                        loadInfo()
                    }
                }
            }

            binding.chatMessagesRv.scrollToPosition(messages.size - 1)
        }


        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                NetworkConnectionLiveData(this@ChatContentActivity).observe(this@ChatContentActivity) { isNetworkAvailable ->
                    if (isNetworkAvailable) {
                        viewModel.getChatConversationData(convoid)
                            .enqueue(object : Callback<ChatConversationResponseBody> {
                                override fun onResponse(
                                    call: Call<ChatConversationResponseBody>,
                                    response: Response<ChatConversationResponseBody>
                                ) {
                                    val body = response.body()
                                    if (body != null) {
                                        val data = body.data.toMutableList()
                                        chatMessagesLiveData.postValue(data)
                                    }
                                }

                                override fun onFailure(
                                    call: Call<ChatConversationResponseBody>,
                                    t: Throwable
                                ) {
                                }
                            })
                    }
                }
            }
        }
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

    private fun sendMessage(chatDTO: ChatDTO) {
        val data = Data.Builder()
            .putString("auth",authToken)
            .putString("chatMessage",chatDTO.message)
            .putString("type",chatDTO.type)
            .putString("convoid",chatDTO.convoid)
            .putString("profileid",chatDTO.receiverid)
            .build()

        val worker = OneTimeWorkRequest.Builder(SendMessageRequestService::class.java)
            .setInputData(data)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueue(worker)
    }

    override fun onStop() {
        val items = arrayListOf<Chat>()
        if (chatMessagesAdapter.chatList.isNotEmpty() && existing < chatMessagesAdapter.chatList.size) {
            val save = chatMessagesAdapter.chatList.reversed().subList(existing, chatMessagesAdapter.chatList.size)
            for (chat in save) {
                chat.convoid = convoid
                items.add(chat)
            }
            saveMessage(items)
        }
        super.onStop()
    }

    private fun saveMessage(items: ArrayList<Chat>) {
        val data = Data.Builder()
            .putString("chatMessages",Gson().toJson(items))
            .build()

        SaveMessagesService.setApplication(application)
        val worker = OneTimeWorkRequest.Builder(SaveMessagesService::class.java)
            .setInputData(data)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        WorkManager.getInstance(this).enqueue(worker)
    }
}