package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChatDTO(
    @SerializedName("itemid")
    val itemid: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("receiverid")
    val receiverid: String = "",
    @SerializedName("convoid")
    val convoid: String = "",
    @SerializedName("type")
    val type: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemid)
        parcel.writeString(message)
        parcel.writeString(receiverid)
        parcel.writeString(convoid)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatDTO> {
        override fun createFromParcel(parcel: Parcel): ChatDTO {
            return ChatDTO(parcel)
        }

        override fun newArray(size: Int): Array<ChatDTO?> {
            return arrayOfNulls(size)
        }
    }
}