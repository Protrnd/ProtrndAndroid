package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class PromotionDTO(
    @SerializedName("amount")
    val amount: Int = 0,
    @SerializedName("bannerUrl")
    val bannerUrl: String = "",
    @SerializedName("chargeIntervals")
    val chargeIntervals: String = "week",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("audience")
    val audience: LocationDTO = LocationDTO(),
    @SerializedName("postId")
    val postId: String = "",
    @SerializedName("profileId")
    val profileId: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(bannerUrl)
        parcel.writeString(chargeIntervals)
        parcel.writeString(email)
        parcel.writeParcelable(audience, flags)
        parcel.writeString(postId)
        parcel.writeString(profileId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PromotionDTO> {
        override fun createFromParcel(parcel: Parcel): PromotionDTO {
            return PromotionDTO(parcel)
        }

        override fun newArray(size: Int): Array<PromotionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
