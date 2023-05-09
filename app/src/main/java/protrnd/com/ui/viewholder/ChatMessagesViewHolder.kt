package protrnd.com.ui.viewholder

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.responses.TransactionResponseBody
import protrnd.com.databinding.ChatMessageLayoutBinding
import protrnd.com.ui.formatAmount
import protrnd.com.ui.getTimeWithCenterDot
import protrnd.com.ui.viewmodels.ChatViewModel
import protrnd.com.ui.visible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMessagesViewHolder(val view: ChatMessageLayoutBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(
        chat: Chat,
        profile: Profile,
        viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner
    ) {
        if (profile.id == chat.senderid) {
            view.receivedMoney.visible(false)
            view.postReceived.visible(false)
            view.receivedMessageTime.visible(false)
            view.receivedMessage.visible(false)
            when (chat.type) {
                "chat" -> {
                    view.sentMessageTime.text = getTimeWithCenterDot(chat.time)
                    view.sentMessage.text = chat.message
                    view.sentMessage.visible(true)
                    view.sentMessageTime.visible(true)
                    view.postSent.visible(false)
                    view.sentMoney.visible(false)
                }
                "forward" -> {
                    view.sentPostMessage.text = chat.message
                    view.sentPostTime.text = getTimeWithCenterDot(chat.time)
                    view.sentMessage.visible(false)
                    view.sentMessageTime.visible(false)
                    view.postSent.visible(true)
                    view.sentMoney.visible(false)

                    val postLiveData = MutableLiveData<Post>()
                    val postData: LiveData<Post> = postLiveData

                    postData.observe(lifecycleOwner) { post ->
                        Glide.with(view.root)
                            .load(post.uploadurls[0])
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(view.sentPostImage)
                    }

                    val post = MemoryCache.posts.find { posts -> posts.id == chat.itemid }
                    if (post != null) {
                        val postItem: Post = post
                        postLiveData.postValue(postItem)
                    } else {
                        viewModel.viewModelScope.launch {
                            when (val postResult = viewModel.getPost(chat.itemid)) {
                                is Resource.Success -> {
                                    val postItemResult = postResult.value.data
                                    postLiveData.postValue(postItemResult)
                                }
                                else -> {}
                            }
                        }
                    }
                }
                "payment" -> {
                    view.sentMessage.visible(false)
                    view.sentMessageTime.visible(false)
                    view.postSent.visible(false)
                    view.sentMoney.visible(true)
                    view.sentMoneyTime.text = getTimeWithCenterDot(chat.time)

                    val transactionLiveData = MutableLiveData<Transaction>()
                    val transactionData: LiveData<Transaction> = transactionLiveData

                    transactionData.observe(lifecycleOwner) { transaction ->
                        val amount = "₦${transaction.amount.formatAmount()}"
                        view.sentAmount.text = amount
                    }

                    val transaction =
                        MemoryCache.transactionsMap.firstOrNull { transaction -> transaction.id == chat.itemid }
                    if (transaction != null) {
                        val transactionItem: Transaction = transaction
                        transactionLiveData.postValue(transactionItem)
                    } else {
                        viewModel.getTransactionById(chat.itemid)
                            .enqueue(object : Callback<TransactionResponseBody> {
                                override fun onResponse(
                                    call: Call<TransactionResponseBody>,
                                    response: Response<TransactionResponseBody>
                                ) {
                                    val body = response.body()!!
                                    val transactionItem: Transaction = body.data
                                    transactionLiveData.postValue(transactionItem)
                                    val result = MemoryCache.transactionsMap
                                    result.add(transactionItem)
                                    MemoryCache.transactionsMap = result
                                }

                                override fun onFailure(
                                    call: Call<TransactionResponseBody>,
                                    t: Throwable
                                ) {
                                }
                            })
                    }
                }
            }
        } else {
            view.sentMessage.visible(false)
            view.sentMessageTime.visible(false)
            view.postSent.visible(false)
            view.sentMoney.visible(false)
            when (chat.type) {
                "chat" -> {
                    view.receivedMessageTime.text = getTimeWithCenterDot(chat.time)
                    view.receivedMessage.text = chat.message
                    view.receivedMessage.visible(true)
                    view.receivedMessageTime.visible(true)
                    view.postReceived.visible(false)
                    view.receivedMoney.visible(false)
                }
                "forward" -> {
                    view.receivePostTime.text = getTimeWithCenterDot(chat.time)
                    view.receivedMessage.visible(false)
                    view.receivedMessageTime.visible(false)
                    view.postReceived.visible(true)
                    view.receivedMoney.visible(false)
                    view.postMessageReceived.text = chat.message
                    val postLiveData = MutableLiveData<Post>()
                    val postData: LiveData<Post> = postLiveData

                    postData.observe(lifecycleOwner) { post ->
                        Glide.with(view.root)
                            .load(post.uploadurls[0])
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(view.postImageReceived)
                    }

                    val post = MemoryCache.posts.find { posts -> posts.id == chat.itemid }
                    if (post != null) {
                        val postItem: Post = post
                        postLiveData.postValue(postItem)
                    } else {
                        viewModel.viewModelScope.launch {
                            when (val postResult = viewModel.getPost(chat.itemid)) {
                                is Resource.Success -> {
                                    val postItemResult = postResult.value.data
                                    postLiveData.postValue(postItemResult)
                                }
                                else -> {}
                            }
                        }
                    }
                }
                "payment" -> {
                    view.receivedMoneyTime.text = getTimeWithCenterDot(chat.time)
                    view.receivedMessage.visible(false)
                    view.receivedMessageTime.visible(false)
                    view.postReceived.visible(false)
                    view.receivedMoney.visible(true)
                    val transactionLiveData = MutableLiveData<Transaction>()
                    val transactionData: LiveData<Transaction> = transactionLiveData

                    transactionData.observe(lifecycleOwner) { transaction ->
                        val amount = "₦${transaction.amount.formatAmount()}"
                        view.receivedAmount.text = amount
                    }

                    val transaction =
                        MemoryCache.transactionsMap.firstOrNull { transaction -> transaction.id == chat.itemid }
                    if (transaction != null) {
                        val transactionItem: Transaction = transaction
                        transactionLiveData.postValue(transactionItem)
                    } else {
                        viewModel.getTransactionById(chat.itemid)
                            .enqueue(object : Callback<TransactionResponseBody> {
                                override fun onResponse(
                                    call: Call<TransactionResponseBody>,
                                    response: Response<TransactionResponseBody>
                                ) {
                                    val body = response.body()!!
                                    val transactionItem: Transaction = body.data
                                    transactionLiveData.postValue(transactionItem)
                                    val result = MemoryCache.transactionsMap
                                    result.add(transactionItem)
                                    MemoryCache.transactionsMap = result
                                }

                                override fun onFailure(
                                    call: Call<TransactionResponseBody>,
                                    t: Throwable
                                ) {
                                }
                            })
                    }
                }
            }
        }
    }
}