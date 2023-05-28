package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "ConversationId")
data class ConversationId (
    @PrimaryKey
    @SerializedName("profileid")
    var profileid: String = "",
    @SerializedName("convoid")
    var convoid: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(profileid)
        parcel.writeString(convoid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConversationId> {
        override fun createFromParcel(parcel: Parcel): ConversationId {
            return ConversationId(parcel)
        }

        override fun newArray(size: Int): Array<ConversationId?> {
            return arrayOfNulls(size)
        }
    }
}