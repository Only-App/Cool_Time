package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmRepository(private val alarmDao : AlarmDAO) {
    val allAlarm : LiveData<List<Alarm>> = alarmDao.getAll()
    suspend fun insert(alarm : Alarm){
        withContext(Dispatchers.IO){ alarmDao.insertAlarm(alarm) }
    }

    suspend fun delete(alarm : Alarm){
        withContext(Dispatchers.IO){alarmDao.deleteAlarm(alarm) }
    }

    suspend fun update(alarm: Alarm){
        withContext(Dispatchers.IO){ alarmDao.updateAlarm(alarm) }
    }

}