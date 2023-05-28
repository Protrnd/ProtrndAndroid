package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Promotion(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("audience")
    val audience: Location,
    @SerializedName("bannerurl")
    val bannerurl: String,
    @SerializedName("chargeintervals")
    val chargeintervals: String,
    @SerializedName("clicks")
    val clicks: Int,
    @SerializedName("createdat")
    val createdat: String,
    @SerializedName("disabled")
    val disabled: Boolean,
    @SerializedName("email")
    val email: String,
    @SerializedName("expirydate")
    val expirydate: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("postid")
    val postid: String,
    @SerializedName("profileid")
    val profileid: String,
    @SerializedName("views")
    val views: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeParcelable(audience, flags)
        parcel.writeString(bannerurl)
        parcel.writeString(chargeintervals)
        parcel.writeInt(clicks)
        parcel.writeString(createdat)
        parcel.writeByte(if (disabled) 1 else 0)
        parcel.writeString(email)
        parcel.writeString(expirydate)
        parcel.writeString(id)
        parcel.writeString(identifier)
        parcel.writeString(postid)
        parcel.writeString(profileid)
        parcel.writeInt(views)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Promotion> {
        override fun createFromParcel(parcel: Parcel): Promotion {
            return Promotion(parcel)
        }

        override fun newArray(size: Int): Array<Promotion?> {
            return arrayOfNulls(size)
        }
    }
}