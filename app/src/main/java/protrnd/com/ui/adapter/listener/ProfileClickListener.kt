package protrnd.com.ui.adapter.listener

import protrnd.com.data.models.Profile
import protrnd.com.ui.viewholder.ProfileTagViewHolder

interface ProfileClickListener {
    fun profileClick(holder: ProfileTagViewHolder? = null, position: Int, profile: Profile)
}