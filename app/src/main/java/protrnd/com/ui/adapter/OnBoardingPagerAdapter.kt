package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.OnboardingLayoutBinding
import protrnd.com.ui.viewholder.OnBoardingViewHolder

class OnBoardingPagerAdapter : RecyclerView.Adapter<OnBoardingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OnBoardingViewHolder(
        OnboardingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
        holder.bind(position)
    }

}