package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Posts")
data class Post(
    @SerializedName("acceptgift")
    val acceptgift: Boolean = false,
    @SerializedName("caption")
    val caption: String = "",
    @SerializedName("disabled")
    val disabled: Boolean = false,
    @SerializedName("id")
    @PrimaryKey val id: String = "",
    @SerializedName("identifier")
    val identifier: String = "",
    @SerializedName("location")
    val location: Location = Location(),
    @SerializedName("profileid")
    val profileid: String = "",
    @SerializedName("time")
    val time: String = "",
    @SerializedName("uploadurls")
    val uploadurls: List<String> = arrayListOf(),
    @SerializedName("tags")
    val tags: List<String> = arrayListOf()
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
        parcel.createStringArrayList()!!,
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
        parcel.writeStringList(tags)
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