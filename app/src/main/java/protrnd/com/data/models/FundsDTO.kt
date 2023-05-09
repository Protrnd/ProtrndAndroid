package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class FundsDTO(
    @SerializedName("amount")
    var amount: Double = 0.0,
    @SerializedName("profileid")
    var profileid: String = "",
    @SerializedName("fromid")
    var fromid: String = "",
    @SerializedName("reference")
    var reference: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(amount)
        parcel.writeString(profileid)
        parcel.writeString(fromid)
        parcel.writeString(reference)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FundsDTO> {
        override fun createFromParcel(parcel: Parcel): FundsDTO {
            return FundsDTO(parcel)
        }

        override fun newArray(size: Int): Array<FundsDTO?> {
            return arrayOfNulls(size)
        }
    }
}