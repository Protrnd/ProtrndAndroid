package protrnd.com.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import protrnd.com.R
import protrnd.com.data.repository.ChatRepository
import protrnd.com.databinding.FragmentChatBinding
import protrnd.com.ui.adapter.RecentChatProfilesAdapter
import protrnd.com.ui.adapter.listener.ChatProfileListener
import protrnd.com.ui.base.BaseFragment

class ChatFragment : BaseFragment<ChatViewModel,FragmentChatBinding,ChatRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = RecentChatProfilesAdapter()
        binding.lastMessagesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.lastMessagesRv.adapter = adapter
        adapter.clickListener(object : ChatProfileListener{
            override fun click() {
                startActivity(Intent(requireContext(),ChatContentActivity::class.java))
            }
        })
    }

    override fun getViewModel() = ChatViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChatBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = ChatRepository()
}