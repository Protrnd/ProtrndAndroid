package protrnd.com.ui.adapter.listener

import protrnd.com.data.models.Conversation

interface ChatProfileListener {
    fun click(conversation: Conversation)
}