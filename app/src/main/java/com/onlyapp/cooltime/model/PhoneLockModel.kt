package com.onlyapp.cooltime.model

data class PhoneLockModel(
    val id: Int,
    val totalTime: Long,
    val minTime: Long,
    val lockOn: Int,
    val lockOff: Int,
    val lockDay: Int,
    val startDate: Long,
    val endDate: Long
)