package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PostDTO(
    @SerializedName("caption")
    val caption: String = "",
    @SerializedName("location")
    val location: Location = Location(),
    @SerializedName("uploadurls")
    val uploadurls: List<String> = arrayListOf(),
    @SerializedName("tags")
    val tags: List<String> = arrayListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.createStringArrayList()!!,
        parcel.createStringArrayList()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(caption)
        parcel.writeParcelable(location, flags)
        parcel.writeStringList(uploadurls)
        parcel.writeStringList(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostDTO> {
        override fun createFromParcel(parcel: Parcel): PostDTO {
            return PostDTO(parcel)
        }

        override fun newArray(size: Int): Array<PostDTO?> {
            return arrayOfNulls(size)
        }
    }
}