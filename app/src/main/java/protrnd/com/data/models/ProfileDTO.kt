package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ProfileDTO(
    @SerializedName("accountType")
    val accountType: String,
    @SerializedName("backgroundImageUrl")
    val backgroundImageUrl: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("about")
    val about: String,
    @SerializedName("profileImage")
    val profileImage: String,
    @SerializedName("userName")
    val userName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accountType)
        parcel.writeString(backgroundImageUrl)
        parcel.writeString(email)
        parcel.writeString(fullName)
        parcel.writeString(location)
        parcel.writeString(about)
        parcel.writeString(profileImage)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileDTO> {
        override fun createFromParcel(parcel: Parcel): ProfileDTO {
            return ProfileDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProfileDTO?> {
            return arrayOfNulls(size)
        }
    }
}