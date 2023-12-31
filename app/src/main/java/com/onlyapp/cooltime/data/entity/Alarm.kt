package com.onlyapp.cooltime.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarm")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val name : String,
    val day : Int,
    val time : Int  //time을 원래 Time Class로 하려고 했으나 안돼서 Int 타입으로 변경함
) : Serializable
