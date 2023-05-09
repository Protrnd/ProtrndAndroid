package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class VerifyPromotion(
    @SerializedName("promotion")
    val promotion: PromotionDTO = PromotionDTO(),
    @SerializedName("reference")
    val reference: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(PromotionDTO::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(promotion, flags)
        parcel.writeString(reference)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerifyPromotion> {
        override fun createFromParcel(parcel: Parcel): VerifyPromotion {
            return VerifyPromotion(parcel)
        }

        override fun newArray(size: Int): Array<VerifyPromotion?> {
            return arrayOfNulls(size)
        }
    }
}