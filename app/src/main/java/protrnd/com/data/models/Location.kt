package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Location(
    val cities: List<String> = arrayListOf(),
    val id: String = UUID.randomUUID().toString(),
    val state: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(cities)
        parcel.writeString(id)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }
    }
}