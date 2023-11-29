package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.model.DateStatModel

interface DateStatRepository {
    suspend fun insertDateStat(dateStatModel: DateStatModel)
    suspend fun getDateStat(date : Long) : DateStatModel?
}