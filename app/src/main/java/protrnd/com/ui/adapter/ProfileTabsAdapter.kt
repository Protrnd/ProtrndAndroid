package protrnd.com.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import protrnd.com.ui.profile.ProfilePostsFragment
import protrnd.com.ui.profile.ProfileTaggedFragment

class ProfileTabsAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfilePostsFragment()
            else -> ProfileTaggedFragment()
        }
    }
}