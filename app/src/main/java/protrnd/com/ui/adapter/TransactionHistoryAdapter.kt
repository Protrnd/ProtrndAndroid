package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.TransactionInfoItemBinding
import protrnd.com.ui.adapter.listener.TransactionItemListener
import protrnd.com.ui.viewholder.TransactionHistoryViewHolder

class TransactionHistoryAdapter : RecyclerView.Adapter<TransactionHistoryViewHolder>() {

    lateinit var listener: TransactionItemListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = TransactionHistoryViewHolder(
        TransactionInfoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = 10

    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
    }

    fun click(listener: TransactionItemListener) {
        this.listener = listener
    }
}