package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class IntDataResponseBody(
    @SerializedName("data")
    val `data`: Double,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(data)
        parcel.writeString(message)
        parcel.writeInt(statusCode)
        parcel.writeByte(if (successful) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IntDataResponseBody> {
        override fun createFromParcel(parcel: Parcel): IntDataResponseBody {
            return IntDataResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<IntDataResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}