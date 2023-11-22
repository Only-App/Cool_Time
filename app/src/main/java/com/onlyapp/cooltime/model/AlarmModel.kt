package com.onlyapp.cooltime.model

import java.io.Serializable

data class AlarmModel(
    val id : Int = 0,
    val name : String,
    val day : Int,
    val time : Int
) : Serializable
