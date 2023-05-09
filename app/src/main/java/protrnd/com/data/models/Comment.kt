package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("uploadid")
    val uploadid: String,
    @SerializedName("userid")
    val userid: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeString(time)
        parcel.writeString(uploadid)
        parcel.writeString(userid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}