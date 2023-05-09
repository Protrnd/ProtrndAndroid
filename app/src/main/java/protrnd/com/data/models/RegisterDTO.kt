package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class RegisterDTO(
    @SerializedName("accountType")
    var accountType: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("fullName")
    var fullName: String = "",
    @SerializedName("password")
    var password: String = "",
    @SerializedName("userName")
    var userName: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accountType)
        parcel.writeString(email)
        parcel.writeString(fullName)
        parcel.writeString(password)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RegisterDTO> {
        override fun createFromParcel(parcel: Parcel): RegisterDTO {
            return RegisterDTO(parcel)
        }

        override fun newArray(size: Int): Array<RegisterDTO?> {
            return arrayOfNulls(size)
        }
    }
}