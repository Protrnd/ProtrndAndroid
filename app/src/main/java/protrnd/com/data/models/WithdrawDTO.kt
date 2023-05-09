package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class WithdrawDTO(
    @SerializedName("account")
    val account: AccountDTO = AccountDTO(),
    @SerializedName("amount")
    val amount: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(AccountDTO::class.java.classLoader)!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(account, flags)
        parcel.writeInt(amount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WithdrawDTO> {
        override fun createFromParcel(parcel: Parcel): WithdrawDTO {
            return WithdrawDTO(parcel)
        }

        override fun newArray(size: Int): Array<WithdrawDTO?> {
            return arrayOfNulls(size)
        }
    }
}