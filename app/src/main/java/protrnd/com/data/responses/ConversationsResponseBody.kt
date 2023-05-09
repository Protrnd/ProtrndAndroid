package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Conversation

data class ConversationsResponseBody(
    @SerializedName("data")
    val `data`: List<Conversation>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Conversation)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeInt(statusCode)
        parcel.writeByte(if (successful) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConversationsResponseBody> {
        override fun createFromParcel(parcel: Parcel): ConversationsResponseBody {
            return ConversationsResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<ConversationsResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}
