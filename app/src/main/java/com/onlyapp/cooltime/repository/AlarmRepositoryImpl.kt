package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.data.dao.AlarmDao
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.model.AlarmModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(private val alarmDao: AlarmDao) : AlarmRepository {
    override suspend fun insert(alarmModel: AlarmModel): Long {
        val alarmEntity = Alarm(
            alarmModel.id,
            alarmModel.name,
            alarmModel.day,
            alarmModel.time
        )
        return alarmDao.insertAlarm(alarmEntity)
    }

    override suspend fun delete(alarmModel: AlarmModel) {
        val alarmEntity = Alarm(
            alarmModel.id,
            alarmModel.name,
            alarmModel.day,
            alarmModel.time
        )
        alarmDao.deleteAlarm(alarmEntity)
    }

    override suspend fun update(alarmModel: AlarmModel) {
        val alarmEntity = Alarm(
            alarmModel.id,
            alarmModel.name,
            alarmModel.day,
            alarmModel.time
        )
        alarmDao.updateAlarm(alarmEntity)
    }

    override suspend fun getAllFlow(): Flow<List<AlarmModel>> {
        return alarmDao.getAllFlow().map {
            it.map { alarmEntity ->
                AlarmModel(
                    alarmEntity.id,
                    alarmEntity.name,
                    alarmEntity.day,
                    alarmEntity.time,
                    ""
                )
            }
        }
    }

    override suspend fun getAlarm(id: Int): AlarmModel {
        val alarmEntity = alarmDao.getAlarm(id)
        return AlarmModel(
            alarmEntity.id,
            alarmEntity.name,
            alarmEntity.day,
            alarmEntity.time,
            ""
        )
    }
}