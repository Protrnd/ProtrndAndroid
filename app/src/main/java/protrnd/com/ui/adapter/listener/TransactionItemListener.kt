package protrnd.com.ui.adapter.listener

import protrnd.com.data.models.Transaction

interface TransactionItemListener {
    fun click(transaction: Transaction)
}