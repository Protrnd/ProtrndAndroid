package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CommentDTO(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("postid")
    val postid: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(postid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommentDTO> {
        override fun createFromParcel(parcel: Parcel): CommentDTO {
            return CommentDTO(parcel)
        }

        override fun newArray(size: Int): Array<CommentDTO?> {
            return arrayOfNulls(size)
        }
    }
}