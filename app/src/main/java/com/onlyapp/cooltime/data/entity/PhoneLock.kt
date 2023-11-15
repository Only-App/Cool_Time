package com.onlyapp.cooltime.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable


//time과 date 관련 변수는 전부 Long 타입으로 함
@Entity(tableName = "phone_lock" )
data class PhoneLock(
    @PrimaryKey(autoGenerate = true)
    val id : Int  = 0,
    // app_list 는 다른 테이블로 관리
    @SerializedName("total_time") val totalTime : Long,
    @SerializedName("min_time") val minTime : Long,
    @SerializedName("lock_on") val lockOn : Int,
    @SerializedName("lock_off") val lockOff : Int,
    @SerializedName("lock_day") val lockDay : Int,
    @SerializedName("start_date") val startDate : Long,
    @SerializedName("end_date") val endDate : Long
) : Serializable
