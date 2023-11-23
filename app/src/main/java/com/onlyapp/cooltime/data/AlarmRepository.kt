package com.onlyapp.cooltime.data

import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun insert(alarm: Alarm): Long

    suspend fun delete(alarm: Alarm)

    suspend fun update(alarm: Alarm)

    suspend fun getAllFlow(): Flow<List<Alarm>>

    suspend fun getAlarm(id: Int): Alarm
}