package protrnd.com.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import protrnd.com.ui.notification.AllNotificationFragment
import protrnd.com.ui.notification.TransactionNotificationFragment

class NotificationTabAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllNotificationFragment()
//            1 -> SocialsNotificationFragment()
            else -> TransactionNotificationFragment()
        }
    }
}