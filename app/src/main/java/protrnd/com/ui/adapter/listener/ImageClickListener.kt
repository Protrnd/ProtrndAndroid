package protrnd.com.ui.adapter.listener

import android.net.Uri

interface ImageClickListener {
    fun imageClickListener(imageUri: Uri, position: Int)
}