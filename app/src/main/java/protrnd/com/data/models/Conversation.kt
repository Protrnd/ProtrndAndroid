package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Conversations")
data class Conversation(
    @SerializedName("id")
    @PrimaryKey val id: String,
    @SerializedName("receiverId")
    val receiverId: String,
    @SerializedName("senderid")
    val senderid: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("recentMessage")
    val recentMessage: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(receiverId)
        parcel.writeString(senderid)
        parcel.writeString(time)
        parcel.writeString(recentMessage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }
}