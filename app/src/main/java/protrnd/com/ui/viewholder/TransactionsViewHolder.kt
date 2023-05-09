package protrnd.com.ui.viewholder

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
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
        val mutable = MutableLiveData<String>()
        val live: LiveData<String> = mutable
        view.transactionAmount.text = amount
        live.observe(lifecycleOwner) {
            view.profileId.text = it
        }
        if (currentProfile.id == transaction.profileid) {
            val profile = MemoryCache.profiles[transaction.receiverid]
            if (profile != null) {
                mutable.postValue("@${profile.username}")
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    when (val profileResult = viewModel.getProfileById(transaction.receiverid)) {
                        is Resource.Success -> {
                            mutable.postValue("@${profileResult.value.data.username}")
                            MemoryCache.profiles[transaction.profileid] = profileResult.value.data
                        }
                        else -> {}
                    }
                }
            }
        } else {
            val profile = MemoryCache.profiles[transaction.profileid]
            if (profile != null) {
                mutable.postValue("@${profile.username}")
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    when (val profileResult = viewModel.getProfileById(transaction.profileid)) {
                        is Resource.Success -> {
                            mutable.postValue("@${profileResult.value.data.username}")
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