package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class QrCodeContent(
    @SerializedName("amount")
    var amount: Int = 0,
    @SerializedName("profile")
    var profile: Profile,
    @SerializedName("time")
    val time: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Profile::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeParcelable(profile, flags)
        parcel.writeString(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QrCodeContent> {
        override fun createFromParcel(parcel: Parcel): QrCodeContent {
            return QrCodeContent(parcel)
        }

        override fun newArray(size: Int): Array<QrCodeContent?> {
            return arrayOfNulls(size)
        }
    }
}