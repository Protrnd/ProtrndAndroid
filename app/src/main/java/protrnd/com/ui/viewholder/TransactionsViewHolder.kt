package protrnd.com.ui.viewholder

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import protrnd.com.data.models.Profile
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.TransactionInfoItemBinding
import protrnd.com.ui.formatAmount
import protrnd.com.ui.getTimeWithCenterDot
import protrnd.com.ui.viewmodels.PaymentViewModel

class TransactionsViewHolder(val view: TransactionInfoItemBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(
        transaction: Transaction,
        viewModel: PaymentViewModel,
        lifecycleOwner: LifecycleOwner,
        currentProfile: Profile
    ) {
        var amount = transaction.amount.formatAmount()
        if (transaction.purpose.contains("Support sent"))
            amount = "-$amount"
        if (amount.startsWith("-")) {
            amount = amount.replace("-", "")
            amount = "-₦$amount"
            view.transactionAmount.setTextColor(Color.parseColor("#FF0C08"))
        } else {
            amount = "₦$amount"
            view.transactionAmount.setTextColor(Color.parseColor("#13101F"))
        }
        val mutable = MutableLiveData<Profile>()
        val live: LiveData<Profile> = mutable
        view.transactionAmount.text = amount
        live.observe(lifecycleOwner) {profile ->
            val username = "@${profile.username}"
            view.profileId.text = username
            Glide.with(view.root)
                .load(profile.profileimg)
                .into(view.profileTrxImage)
        }
        if (currentProfile.id == transaction.profileid) {
            val profile = MemoryCache.profiles[transaction.receiverid]
            if (profile != null) {
                val p: Profile = profile
                mutable.postValue(p)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    when (val profileResult = viewModel.getProfileById(transaction.receiverid)) {
                        is Resource.Success -> {
                            mutable.postValue(profileResult.value.data)
                            MemoryCache.profiles[transaction.profileid] = profileResult.value.data
                        }
                        else -> {}
                    }
                }
            }
        } else {
            val profile = MemoryCache.profiles[transaction.profileid]
            if (profile != null) {
                val p: Profile = profile
                mutable.postValue(p)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    when (val profileResult = viewModel.getProfileById(transaction.profileid)) {
                        is Resource.Success -> {
                            mutable.postValue(profileResult.value.data)
                            MemoryCache.profiles[transaction.profileid] = profileResult.value.data
                        }
                        else -> {}
                    }
                }
            }
        }

        view.transactionDate.text = getTimeWithCenterDot(transaction.createdat)
    }
}