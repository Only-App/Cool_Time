package com.onlyapp.cooltime.model

data class DateStatModel(
    val date : Long,
    val appStatList : List<Pair<String, Long>>,
    val hourStatList : List<Long>
)
