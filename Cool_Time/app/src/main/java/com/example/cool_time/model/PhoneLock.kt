package com.example.cool_time.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


//time과 date 관련 변수는 전부 Long 타입으로 함
@Entity(tableName = "phone_lock" )
data class PhoneLock(
    @PrimaryKey(autoGenerate = true)
    var id : Int  = 0,
    var app_list : List<String>,
    var total_time : Long,
    var min_time : Long,
    var lock_on : Int,
    var lock_off : Int,
    var lock_day : Int,
    var start_date : Long,
    var end_date : Long
) : Serializable
