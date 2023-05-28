package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Profiles")
data class Profile(
    @SerializedName("acctype")
    var acctype: String = "",
    @SerializedName("bgimg")
    var bgimg: String = "",
    @SerializedName("disabled")
    var disabled: Boolean = false,
    @SerializedName("email")
    var email: String = "",
    @SerializedName("fullname")
    var fullname: String = "",
    @PrimaryKey
    @SerializedName("id")
    var id: String = "",
    @SerializedName("identifier")
    var identifier: String = "",
    @SerializedName("location")
    var location: String? = "",
    @SerializedName("about")
    var about: String? = "",
    @SerializedName("profileimg")
    var profileimg: String = "",
    @SerializedName("regdate")
    var regdate: String = "",
    @SerializedName("username")
    var username: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(acctype)
        parcel.writeString(bgimg)
        parcel.writeByte(if (disabled) 1 else 0)
        parcel.writeString(email)
        parcel.writeString(fullname)
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeString(location)
        parcel.writeString(about)
        parcel.writeString(profileimg)
        parcel.writeString(regdate)
        parcel.writeString(username)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }
}