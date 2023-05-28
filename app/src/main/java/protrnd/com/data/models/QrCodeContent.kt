package protrnd.com.data.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import com.preference.BuildConfig

@Keep
data class QrCodeContent(
    @SerializedName("amount")
    var amount: Int = 0,
    @SerializedName("profile")
    var profile: Profile = Profile(),
    @SerializedName("time")
    val time: String = "",
    @SerializedName("isindebugmode")
    val isInDebugMode: Boolean = true
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Profile::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeParcelable(profile, flags)
        parcel.writeString(time)
        parcel.writeByte(if (isInDebugMode) 1 else 0)
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