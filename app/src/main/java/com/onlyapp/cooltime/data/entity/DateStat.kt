package com.onlyapp.cooltime.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity("date_stat")
data class DateStat(
    @PrimaryKey
    @SerializedName("date") val date : Long,
    @SerializedName("app_stat_list") val appStatList : List<Pair<String, Long>>,
    @SerializedName("hour_stat_list") val hourStatList : List<Long>
)
