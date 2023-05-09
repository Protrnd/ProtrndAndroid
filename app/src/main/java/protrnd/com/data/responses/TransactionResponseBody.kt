package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Transaction

data class TransactionResponseBody(
    @SerializedName("data")
    val `data`: Transaction,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Transaction::class.java.classLoader)!!,
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

    companion object CREATOR : Parcelable.Creator<TransactionResponseBody> {
        override fun createFromParcel(parcel: Parcel): TransactionResponseBody {
            return TransactionResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<TransactionResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}
