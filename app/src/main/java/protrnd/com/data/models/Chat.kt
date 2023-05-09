package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("itemid")
    val itemid: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("receiverid")
    val receiverid: String = "",
    @SerializedName("seen")
    val seen: Boolean = false,
    @SerializedName("senderid")
    val senderid: String = "",
    @SerializedName("time")
    val time: String = "",
    @SerializedName("type")
    val type: String = "chat"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(itemid)
        parcel.writeString(message)
        parcel.writeString(receiverid)
        parcel.writeByte(if (seen) 1 else 0)
        parcel.writeString(senderid)
        parcel.writeString(time)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }
    }
}