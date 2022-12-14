package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable

data class Profile(
    var acctype: String = "",
    var bgimg: String = "",
    var disabled: Boolean = false,
    var email: String = "",
    var fullname: String = "",
    var id: String = "",
    var identifier: String = "",
    var location: String? = "",
    var phone: String? = "",
    var profileimg: String = "",
    var regdate: String = "",
    var username: String = ""
): Parcelable {
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
        parcel.writeString(phone)
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