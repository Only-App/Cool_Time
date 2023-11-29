package com.onlyapp.cooltime.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringLongListConverter {
    @TypeConverter
    fun listToJson(list: List<Pair<String, Long>>) : String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToList(value : String) : List<Pair<String, Long>>{
        val listType = object : TypeToken<List<Pair<String, Long>>>() {}.type
        return Gson().fromJson(value, listType)
    }
}