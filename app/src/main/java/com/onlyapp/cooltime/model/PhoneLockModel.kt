package com.onlyapp.cooltime.model

import java.io.Serializable

data class PhoneLockModel(
    val id: Int = 0,
    val totalTime: Long,
    val minTime: Long,
    val lockOn: Int,
    val lockOff: Int,
    val lockDay: Int,
    val startDate: Long,
    val endDate: Long
) : Serializable