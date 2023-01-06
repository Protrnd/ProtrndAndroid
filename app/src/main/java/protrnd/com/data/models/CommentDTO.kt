package protrnd.com.data.models

import com.google.gson.annotations.SerializedName

data class CommentDTO(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("postid")
    val postid: String
)