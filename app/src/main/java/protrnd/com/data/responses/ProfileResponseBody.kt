package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Profile

data class ProfileResponseBody(
    @SerializedName("data")
    val `data`: Profile = Profile(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 0,
    @SerializedName("successful")
    val successful: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Profile::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(data, flags)
        parcel.writeString(message)
        parcel.writeInt(statusCode)
        parcel.writeByte(if (successful) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileResponseBody> {
        override fun createFromParcel(parcel: Parcel): ProfileResponseBody {
            return ProfileResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<ProfileResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}