package protrnd.com.data.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListTypeConverter {
    @TypeConverter
    fun toList(item: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return if (item != null) {
            Gson().fromJson<List<String>>(item, type)
        } else
            null
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return if (list != null)
            Gson().toJson(list)
        else
            null
    }

    @TypeConverter
    fun toLocation(item: String?): Location? {
        val type = object : TypeToken<Location>() {}.type
        return if (item != null) {
            Gson().fromJson<Location>(item, type)
        } else
            null
    }

    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return if (location != null)
            Gson().toJson(location)
        else
            null
    }
}