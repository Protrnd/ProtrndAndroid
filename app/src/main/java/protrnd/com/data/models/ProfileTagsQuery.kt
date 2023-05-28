package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ProfileTagsQuery(
    @SerializedName("page")
    var page: Int = 1,
    @SerializedName("profileid")
    var profileid: String = ""
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

    companion object CREATOR : Parcelable.Creator<ProfileTagsQuery> {
        override fun createFromParcel(parcel: Parcel): ProfileTagsQuery {
            return ProfileTagsQuery(parcel)
        }

        override fun newArray(size: Int): Array<ProfileTagsQuery?> {
            return arrayOfNulls(size)
        }
    }
}
