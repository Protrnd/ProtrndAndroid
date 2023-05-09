package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AccountDTO(
    @SerializedName("accountname")
    val accountName: String = "",
    @SerializedName("accountnumber")
    val accountNumber: String = "",
    @SerializedName("bankname")
    val bankName: String = "",
    @SerializedName("profileid")
    val profileId: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accountName)
        parcel.writeString(accountNumber)
        parcel.writeString(bankName)
        parcel.writeString(profileId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccountDTO> {
        override fun createFromParcel(parcel: Parcel): AccountDTO {
            return AccountDTO(parcel)
        }

        override fun newArray(size: Int): Array<AccountDTO?> {
            return arrayOfNulls(size)
        }
    }
}