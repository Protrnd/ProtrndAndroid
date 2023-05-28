package protrnd.com.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SupportDTO(
    @SerializedName("amount")
    val amount: Int = 0,
    @SerializedName("postId")
    val postId: String = "",
    @SerializedName("receiverId")
    val receiverId: String = "",
    @SerializedName("reference")
    val reference: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(amount)
        parcel.writeString(postId)
        parcel.writeString(receiverId)
        parcel.writeString(reference)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SupportDTO> {
        override fun createFromParcel(parcel: Parcel): SupportDTO {
            return SupportDTO(parcel)
        }

        override fun newArray(size: Int): Array<SupportDTO?> {
            return arrayOfNulls(size)
        }
    }
}