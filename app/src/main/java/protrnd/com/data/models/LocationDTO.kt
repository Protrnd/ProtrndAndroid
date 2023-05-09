package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class LocationDTO(
    @SerializedName("city")
    val city: String = "",
    @SerializedName("state")
    val state: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(city)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationDTO> {
        override fun createFromParcel(parcel: Parcel): LocationDTO {
            return LocationDTO(parcel)
        }

        override fun newArray(size: Int): Array<LocationDTO?> {
            return arrayOfNulls(size)
        }
    }
}