package protrnd.com.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import protrnd.com.data.models.Conversation
import protrnd.com.data.models.ConversationId
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.ChatApi
import protrnd.com.data.repository.ChatRepository
import protrnd.com.data.responses.ConversationsResponseBody
import protrnd.com.databinding.FragmentChatBinding
import protrnd.com.ui.adapter.RecentChatProfilesAdapter
import protrnd.com.ui.adapter.listener.ChatProfileListener
import protrnd.com.ui.base.BaseFragment
import protrnd.com.ui.viewmodels.ChatViewModel
import protrnd.com.ui.visible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : BaseFragment<ChatViewModel, FragmentChatBinding, ChatRepository>() {

    lateinit var adapter: RecentChatProfilesAdapter
    val chatconversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = chatconversations

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecentChatProfilesAdapter(listOf(), viewModel, currentUserProfile)
        binding.lastMessagesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.lastMessagesRv.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val convos = viewModel.getConvos()?.first()
            if (convos != null) {
                val conversations: List<Conversation> = convos
                chatconversations.postValue(conversations)
            }
        }

        updateConversations()

        adapter.clickListener(object : ChatProfileListener {
            override fun click(conversation: Conversation) {
                startActivity(Intent(requireContext(), ChatContentActivity::class.java).apply {
                    putExtra("convoid",conversation.id)
                    if (conversation.senderid == currentUserProfile.id)
                        putExtra("profileid", conversation.receiverId)
                    else
                        putExtra("profileid", conversation.senderid)
                })
            }
        })

        binding.refreshLayout.setOnRefreshListener {
            updateConversations()
        }

        conversations.observe(viewLifecycleOwner) { convos ->
            binding.messagesResults.visible(convos.isEmpty())
            adapter.conversations = convos
            adapter.notifyItemRangeChanged(0, convos.size)
            if (binding.refreshLayout.isRefreshing)
                binding.refreshLayout.isRefreshing = false
        }
    }

    private fun updateConversations() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            viewModel.getConversations().enqueue(object : Callback<ConversationsResponseBody> {
                override fun onResponse(
                    call: Call<ConversationsResponseBody>,
                    response: Response<ConversationsResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()!!.data
                        chatconversations.postValue(data)
                    }
                }

                override fun onFailure(call: Call<ConversationsResponseBody>, t: Throwable) {
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        updateConversations()
    }

    override fun onStop() {
        viewModel.saveConversations(adapter.conversations)
        super.onStop()
    }

    override fun getViewModel() = ChatViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChatBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): ChatRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ChatApi::class.java, token)
        val dao = protrndAPIDataSource.provideChatDatabase(requireActivity().application)
        val idDao = protrndAPIDataSource.provideConversationIdDatabase(requireActivity().application)
        val convoDb = protrndAPIDataSource.provideConversationDatabase(requireActivity().application)
        val profileDb = protrndAPIDataSource.provideProfileDatabase(requireActivity().application)
        return ChatRepository(api, dao, conversationIdDb = idDao, conversationDb = convoDb, profileDb = profileDb)
    }
}