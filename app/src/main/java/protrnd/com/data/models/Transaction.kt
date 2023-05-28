package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Transactions")
data class Transaction(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("createdat")
    val createdat: String,
    @SerializedName("id")
    @PrimaryKey val id: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("itemid")
    val itemid: String,
    @SerializedName("profileid")
    val profileid: String,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("receiverid")
    val receiverid: String,
    @SerializedName("trxref")
    val trxref: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
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
        parcel.writeInt(amount)
        parcel.writeString(createdat)
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeString(itemid)
        parcel.writeString(profileid)
        parcel.writeString(purpose)
        parcel.writeString(receiverid)
        parcel.writeString(trxref)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}