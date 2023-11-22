package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface AlarmRepository {
    suspend fun insert(alarm: Alarm): Long

    suspend fun delete(alarm: Alarm)

    suspend fun update(alarm: Alarm)

    suspend fun getAllFlow(): Flow<List<Alarm>>

    suspend fun getAlarm(id: Int): Alarm
}