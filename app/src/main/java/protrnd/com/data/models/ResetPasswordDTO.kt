package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ResetPasswordDTO (
    @SerializedName("otpHash")
    val otpHash: String = "",
    @SerializedName("plainText")
    val plainText: String = "",
    @SerializedName("reset")
    val reset: Login = Login()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Login::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(otpHash)
        parcel.writeString(plainText)
        parcel.writeParcelable(reset, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResetPasswordDTO> {
        override fun createFromParcel(parcel: Parcel): ResetPasswordDTO {
            return ResetPasswordDTO(parcel)
        }

        override fun newArray(size: Int): Array<ResetPasswordDTO?> {
            return arrayOfNulls(size)
        }
    }
}