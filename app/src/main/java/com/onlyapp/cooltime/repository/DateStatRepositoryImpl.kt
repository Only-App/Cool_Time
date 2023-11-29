package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.data.dao.DateStatDao
import com.onlyapp.cooltime.data.entity.DateStat
import com.onlyapp.cooltime.model.DateStatModel

class DateStatRepositoryImpl(private val dateStatDao: DateStatDao) : DateStatRepository {
    override suspend fun insertDateStat(dateStatModel: DateStatModel) {
        val dateStatEntity = DateStat(
            dateStatModel.date,
            dateStatModel.appStatList,
            dateStatModel.hourStatList
        )
        dateStatDao.insertDateStat(dateStatEntity)
    }
    override suspend fun getDateStat(date: Long): DateStatModel? {
        val dateStatEntity = dateStatDao.getDateStat(date)
        return if(dateStatEntity == null) null
        else DateStatModel(dateStatEntity.date,
            dateStatEntity.appStatList,
            dateStatEntity.hourStatList)
    }
}