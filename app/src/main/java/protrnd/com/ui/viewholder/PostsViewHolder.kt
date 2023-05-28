package protrnd.com.ui.viewholder

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.*
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.viewmodels.HomeViewModel

class PostsViewHolder(val view: PostItemBinding) : RecyclerView.ViewHolder(view.root) {
    @SuppressLint("SetTextI18n")
    fun bind(
        activity: BaseActivity<*, *, *>,
        item: Post,
        postOwnerProfile: Profile? = null,
        currentProfile: Profile,
        viewModel: HomeViewModel? = null
    ) {
        //loads image from network using coil extension function
        bindPostDetails(
            tabLayout = view.tabLayout,
            fullnameTv = view.fullname,
            locationTv = view.location,
            captionTv = view.captionTv,
            post = item,
            profileImage = view.postOwnerImage,
            imagesPager = view.imagesViewPager,
            postOwnerProfile = postOwnerProfile,
            timeText = view.time,
            activity = activity,
            viewModel = viewModel,
            currentProfile = currentProfile,
            moreInfoBtn = view.moreInfo
        )

        view.shareBtn.setOnClickListener {
            itemView.context.showFeatureComingSoonDialog()
        }

        if (currentProfile != postOwnerProfile) {
            view.promoteSupport.text = "Support"
        }

        view.readMoreTv.visible(view.captionTv.lineCount > 3)
    }
}