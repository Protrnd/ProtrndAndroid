package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Profile
import protrnd.com.databinding.ProfileTagBinding
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.viewholder.ProfileTagViewHolder

class ProfileTagAdapter(private val profiles: List<Profile>): RecyclerView.Adapter<ProfileTagViewHolder>() {
    lateinit var positionClickListener: ProfileClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProfileTagViewHolder(
        ProfileTagBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ProfileTagViewHolder, position: Int) {
        holder.bind(profiles[position])
        holder.view.root.setOnClickListener {
            positionClickListener.profileClick(profiles[position])
        }
    }

    override fun getItemCount() = profiles.size

    fun clickPosition(positionClickListener: ProfileClickListener){
        this.positionClickListener = positionClickListener
    }
}