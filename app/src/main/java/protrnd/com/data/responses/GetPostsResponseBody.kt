package protrnd.com.data.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import protrnd.com.data.models.Post

data class GetPostsResponseBody(
    @SerializedName("data")
    val `data`: List<Post>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("successful")
    val successful: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Post)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
        parcel.writeString(message)
        parcel.writeInt(statusCode)
        parcel.writeByte(if (successful) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GetPostsResponseBody> {
        override fun createFromParcel(parcel: Parcel): GetPostsResponseBody {
            return GetPostsResponseBody(parcel)
        }

        override fun newArray(size: Int): Array<GetPostsResponseBody?> {
            return arrayOfNulls(size)
        }
    }
}