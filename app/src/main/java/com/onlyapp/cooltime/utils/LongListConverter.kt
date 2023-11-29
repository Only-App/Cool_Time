package com.onlyapp.cooltime.utils

import androidx.room.TypeConverter
import com.google.gson.Gson

class LongListConverter {
    @TypeConverter
    fun listToJson(list: List<Long>) : String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToList(value : String) : List<Long>{
        return Gson().fromJson(value, Array<Long>::class.java).toList()
    }
}