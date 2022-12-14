package protrnd.com.ui.viewholder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.R
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.bindPostDetails
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.promotion.NewPromotionActivity
import protrnd.com.ui.visible

class PostsViewHolder(val view: PostItemBinding): RecyclerView.ViewHolder(view.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Post, postOwnerProfile: Profile? = null, currentProfile: Profile) {
        //loads image from network using coil extension function
        view.bindPostDetails(
            usernameTv = view.username,
            fullnameTv = view.fullname,
            locationTv = view.location,
            captionTv = view.captionTv,
            post = item,
            profileImage = view.postOwnerImage,
            imagesPager = view.imagesViewPager,
            postOwnerProfile = postOwnerProfile!!,
            tabLayout = view.tabLayout,
            timeText = view.timeUploaded
        )

        if (currentProfile != postOwnerProfile) {
            view.promoteText.text = "Support"
            view.promoteText.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.support_ic,0)
        }

        if (view.captionTv.lineCount > 3)
            view.readMoreTv.visible(true)
        else
            view.readMoreTv.visible(false)

        view.postOwnerImage.setOnClickListener {
            it.context.startActivity(
                Intent(it.context,
                    ProfileActivity::class.java).also { intent ->
                intent.putExtra("profile_id",postOwnerProfile.identifier)
            })
        }

        view.promoteBtn.setOnClickListener {
            if(currentProfile == postOwnerProfile) {
                val i = Intent(it.context, NewPromotionActivity::class.java).also { intent ->
                    intent.putExtra("post_details", item)
                }
                it.context.startActivity(i)
            }
        }
    }
}