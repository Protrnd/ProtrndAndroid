package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class PostProfileTagsQuery(
    @SerializedName("page")
    val page: Int = 0,
    @SerializedName("profileid")
    val profileid: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(page)
        parcel.writeString(profileid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostProfileTagsQuery> {
        override fun createFromParcel(parcel: Parcel): PostProfileTagsQuery {
            return PostProfileTagsQuery(parcel)
        }

        override fun newArray(size: Int): Array<PostProfileTagsQuery?> {
            return arrayOfNulls(size)
        }
    }
}