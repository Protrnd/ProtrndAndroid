package protrnd.com.ui.adapter.listener

import protrnd.com.data.models.Promotion

interface PromotionClickListener {
    fun click(position: Int, promotion: Promotion)
}