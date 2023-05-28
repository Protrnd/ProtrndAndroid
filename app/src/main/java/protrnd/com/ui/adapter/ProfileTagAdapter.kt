package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Profile
import protrnd.com.databinding.ProfileTagBinding
import protrnd.com.ui.adapter.listener.ProfileClickListener
import protrnd.com.ui.viewholder.ProfileTagViewHolder
import protrnd.com.ui.visible

class ProfileTagAdapter(var profiles: List<Profile>, var showSendBtn: Boolean = false) :
    RecyclerView.Adapter<ProfileTagViewHolder>() {
    private lateinit var positionClickListener: ProfileClickListener

    fun setProfileList(mProfiles: List<Profile>) {
        if (mProfiles.isNotEmpty()) {
            profiles = mProfiles
        } else {
            val result = DiffUtil.calculateDiff(object :DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return profiles.size
                }

                override fun getNewListSize(): Int {
                    return mProfiles.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return profiles[oldItemPosition].id == mProfiles[newItemPosition].id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return mProfiles[newItemPosition] == profiles[oldItemPosition]
                }
            })
            profiles = mProfiles
            result.dispatchUpdatesTo(this)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProfileTagViewHolder(
        ProfileTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ProfileTagViewHolder, position: Int) {
        holder.bind(profiles[position])
        holder.view.root.setOnClickListener {
            positionClickListener.profileClick(holder, position, profiles[position])
        }

        holder.view.sendBtn.visible(showSendBtn)

        holder.view.sendBtn.setOnClickListener {
            positionClickListener.profileClick(holder, position, profiles[position])
        }
    }

    override fun getItemCount() = profiles.size

    fun clickPosition(positionClickListener: ProfileClickListener) {
        this.positionClickListener = positionClickListener
    }
}