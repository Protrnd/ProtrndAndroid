package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Notifications")
data class Notification(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("item_id")
    val item_id: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("receiverid")
    val receiverid: String,
    @SerializedName("senderid")
    val senderid: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("viewed")
    val viewed: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeString(item_id)
        parcel.writeString(message)
        parcel.writeString(receiverid)
        parcel.writeString(senderid)
        parcel.writeString(time)
        parcel.writeString(type)
        parcel.writeByte(if (viewed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Notification> {
        override fun createFromParcel(parcel: Parcel): Notification {
            return Notification(parcel)
        }

        override fun newArray(size: Int): Array<Notification?> {
            return arrayOfNulls(size)
        }
    }
}