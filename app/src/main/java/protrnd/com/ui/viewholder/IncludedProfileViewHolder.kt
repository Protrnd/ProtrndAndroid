package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Profile
import protrnd.com.databinding.AddedProfileLayoutBinding

class IncludedProfileViewHolder(val view: AddedProfileLayoutBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(profile: Profile) {
        view.profileIncluded.text = profile.fullname
    }
}