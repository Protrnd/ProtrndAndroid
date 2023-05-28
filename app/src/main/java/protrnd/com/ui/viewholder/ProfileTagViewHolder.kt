package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import protrnd.com.R
import protrnd.com.data.models.Profile
import protrnd.com.databinding.ProfileTagBinding

class ProfileTagViewHolder(val view: ProfileTagBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(profile: Profile) {
        if (profile.profileimg.isNotEmpty()) {
            Glide.with(view.root)
                .load(profile.profileimg)
                .circleCrop()
                .into(view.userImage)
        } else {
            view.userImage.setImageResource(R.drawable.round_bg)
        }
        val tagText = "@${profile.username}"
        view.usernameTagTv.text = tagText
        view.fullnameTagTv.text = profile.fullname
    }
}