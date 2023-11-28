package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.model.AlarmModel
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun insert(alarmModel: AlarmModel): Long

    suspend fun delete(alarmModel: AlarmModel)

    suspend fun update(alarmModel: AlarmModel)

    suspend fun getAllFlow(): Flow<List<AlarmModel>>

    suspend fun getAlarm(id: Int): AlarmModel
}