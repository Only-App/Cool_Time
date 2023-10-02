package com.example.cool_time

import androidx.room.TypeConverter
import com.google.gson.Gson


//일반적으로 Room에서 List타입이 허용되지 않아서 TypeConverter 클래스를 구성하여 Type 변환을 할 수 있도록 함
class StringListConverter {
    @TypeConverter
    fun listToJson(list: List<String>) : String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun jsonToList(value : String) : List<String>{
        return Gson().fromJson(value, Array<String>::class.java).toList()
    }
}