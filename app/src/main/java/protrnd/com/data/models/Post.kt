package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable

data class Post(
    val acceptgift: Boolean = false,
    val caption: String = "",
    val disabled: Boolean = false,
    val id: String = "",
    val identifier: String = "",
    val location: Location = Location(),
    val profileid: String = "",
    val time: String = "",
    val uploadurls: List<String> = arrayListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (acceptgift) 1 else 0)
        parcel.writeString(caption)
        parcel.writeByte(if (disabled) 1 else 0)
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeParcelable(location, flags)
        parcel.writeString(profileid)
        parcel.writeString(time)
        parcel.writeStringList(uploadurls)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}