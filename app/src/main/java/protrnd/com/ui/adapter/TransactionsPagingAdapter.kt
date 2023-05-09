package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import protrnd.com.data.models.Profile
import protrnd.com.data.models.Transaction
import protrnd.com.databinding.TransactionInfoItemBinding
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.viewholder.TransactionsViewHolder
import protrnd.com.ui.viewmodels.PaymentViewModel

class TransactionsPagingAdapter(
    val viewModel: PaymentViewModel,
    private val lifecycleOwner: LifecycleOwner,
    val currentProfile: Profile
) : PagingDataAdapter<Transaction, TransactionsViewHolder>(TransactionComparator()) {
    lateinit var transactionClickListener: TransactionItemListener

    class TransactionComparator : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        val transactionData = getItem(position)!!
        holder.bind(transactionData, viewModel, lifecycleOwner, currentProfile)
        holder.view.root.setOnClickListener {
            transactionClickListener.click(transactionData)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        return TransactionsViewHolder(
            TransactionInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun click(transactionItemListener: TransactionItemListener) {
        this.transactionClickListener = transactionItemListener
    }
}