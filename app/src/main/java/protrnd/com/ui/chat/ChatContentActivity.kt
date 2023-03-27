package protrnd.com.ui.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import protrnd.com.R
import protrnd.com.data.repository.ChatRepository
import protrnd.com.databinding.ActivityChatContentBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.finishActivity

class ChatContentActivity : BaseActivity<ActivityChatContentBinding,ChatViewModel,ChatRepository>() {
    override fun getActivityBinding(inflater: LayoutInflater) = ActivityChatContentBinding.inflate(inflater)

    override fun getViewModel() = ChatViewModel::class.java

    override fun getActivityRepository() = ChatRepository()

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)

        setSupportActionBar(binding.chatToolbar)
        val actionBar = supportActionBar!!
        actionBar.title = "Protrnd" //TODO: Change title to profile name
        actionBar.setDisplayHomeAsUpEnabled(true)
        binding.chatToolbar.contentInsetStartWithNavigation = 0
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_ic)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishActivity()
        }
        return true
    }
}