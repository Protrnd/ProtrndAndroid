package protrnd.com.data.pagingsource

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.adapter.listener.PromoteListener
import protrnd.com.ui.adapter.listener.SupportListener
import protrnd.com.ui.viewholder.PostsViewHolder

class TempPostsPager : RecyclerView.Adapter<PostsViewHolder>() {
    private var promoteListener: PromoteListener? = null
    private var supportListener: SupportListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PostsViewHolder(
        PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        if (position % 2 == 0) {
            holder.view.promoteSupport.text = "Support"
        }
        holder.view.promoteSupport.setOnClickListener {
            if (holder.view.promoteSupport.text == "Support")
                supportListener?.click()
            else
                promoteListener?.click()
        }
    }

    fun promotePost(promoteListener: PromoteListener) {
        this.promoteListener = promoteListener
    }

    fun supportPost(supportListener: SupportListener) {
        this.supportListener = supportListener
    }
}