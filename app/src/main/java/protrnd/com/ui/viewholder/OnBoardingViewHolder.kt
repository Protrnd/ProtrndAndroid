package protrnd.com.ui.viewholder

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.OnboardingLayoutBinding
import protrnd.com.ui.setGradient

class OnBoardingViewHolder(val view: OnboardingLayoutBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(position: Int) {
        view.title.setGradient()
        when (position) {
            0 -> {
                view.swipe.visibility = VISIBLE
                view.title.text = "Discover awesome stuff"
                view.button1.isChecked = true
            }
            1 -> {
                view.swipe.visibility = VISIBLE
                view.title.text = "Promote your business"
                view.button2.isChecked = true
            }
            2 -> {
                view.swipe.visibility = GONE
                view.title.text = "Fast and Secure Payment"
                view.button3.isChecked = true
            }
        }
    }
}