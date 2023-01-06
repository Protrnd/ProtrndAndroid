package protrnd.com.ui.viewholder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.R
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.*
import protrnd.com.ui.profile.ProfileActivity

class PostsViewHolder(val view: PostItemBinding) : RecyclerView.ViewHolder(view.root) {
    @SuppressLint("SetTextI18n")
    fun bind(
        activity: Activity,
        item: Post,
        postOwnerProfile: Profile? = null,
        currentProfile: Profile
    ) {
        //loads image from network using coil extension function
        bindPostDetails(
            tabLayout = view.tabLayout,
            fullnameTv = view.fullname,
            usernameTv = view.username,
            locationTv = view.location,
            captionTv = view.captionTv,
            post = item,
            profileImage = view.postOwnerImage,
            imagesPager = view.imagesViewPager,
            postOwnerProfile = postOwnerProfile,
            timeText = view.timeUploaded,
            activity = activity
        )

        view.shareBtn.setOnClickListener {
            itemView.context.showFeatureComingSoonDialog()
        }

        view.sendTextBtn.setOnClickListener {
            itemView.context.showFeatureComingSoonDialog()
        }

        if (currentProfile != postOwnerProfile) {
            view.promoteText.text = "Support"
            view.promoteText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.support_ic, 0)
        }

        if (view.captionTv.lineCount > 3)
            view.readMoreTv.visible(true)
        else
            view.readMoreTv.visible(false)

        view.postOwnerImage.setOnClickListener {
            if (postOwnerProfile != null) {
                it.context.startActivity(
                    Intent(
                        it.context,
                        ProfileActivity::class.java
                    ).also { intent ->
                        intent.putExtra("profile_id", postOwnerProfile.identifier)
                    })
                activity.startAnimation()
            }
        }

        view.promoteBtn.setOnClickListener {
            itemView.context.showFeatureComingSoonDialog()
//            if(currentProfile == postOwnerProfile) {
//                val i = Intent(it.context, NewPromotionActivity::class.java).also { intent ->
//                    intent.putExtra("post_details", item)
//                }
//                it.context.startActivity(i)
//            }
        }
    }
}