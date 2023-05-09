package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Profile
import protrnd.com.databinding.AddedProfileLayoutBinding
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.viewholder.IncludedProfileViewHolder

class IncludedProfilesAdapter(val profiles: List<Profile>) :
    RecyclerView.Adapter<IncludedProfileViewHolder>() {
    lateinit var profileClickListener: ProfileClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = IncludedProfileViewHolder(
        AddedProfileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = profiles.size

    override fun onBindViewHolder(holder: IncludedProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
        holder.view.root.setOnClickListener {
            profileClickListener.profileClick(position = position, profile = profiles[position])
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    fun removeProfile(profileClickListener: ProfileClickListener) {
        this.profileClickListener = profileClickListener
    }
}