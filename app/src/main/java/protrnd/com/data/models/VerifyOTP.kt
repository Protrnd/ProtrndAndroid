package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class VerifyOTP(
    @SerializedName("otpHash")
    var otpHash: String = "",
    @SerializedName("plainText")
    var plainText: String = "",
    @SerializedName("registerDto")
    val registerDto: RegisterDTO? = RegisterDTO(),
    @SerializedName("type")
    val type: String = "jwt"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(RegisterDTO::class.java.classLoader),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(otpHash)
        parcel.writeString(plainText)
        parcel.writeParcelable(registerDto, flags)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerifyOTP> {
        override fun createFromParcel(parcel: Parcel): VerifyOTP {
            return VerifyOTP(parcel)
        }

        override fun newArray(size: Int): Array<VerifyOTP?> {
            return arrayOfNulls(size)
        }
    }
}