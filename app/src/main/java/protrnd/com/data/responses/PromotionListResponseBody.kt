package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Promotion

data class PromotionListResponseBody(
    @SerializedName("data")
    val `data`: List<Promotion>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Promotion)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
        parcel.writeString(message)
        parcel.writeInt(statusCode)
        parcel.writeByte(if (successful) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PromotionListResponseBody> {
        override fun createFromParcel(parcel: Parcel): PromotionListResponseBody {
            return PromotionListResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<PromotionListResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}