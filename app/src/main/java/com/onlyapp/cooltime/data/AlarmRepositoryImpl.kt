package com.onlyapp.cooltime.data

import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AlarmRepositoryImpl(private val alarmDao : AlarmDAO) : AlarmRepository {
    override suspend fun insert(alarm : Alarm) : Long {
        return withContext(Dispatchers.IO){ alarmDao.insertAlarm(alarm) }
    }

    override suspend fun delete(alarm : Alarm){
        withContext(Dispatchers.IO){alarmDao.deleteAlarm(alarm) }
    }

    override suspend fun update(alarm: Alarm){
        withContext(Dispatchers.IO){ alarmDao.updateAlarm(alarm) }
    }

    override suspend fun getAllFlow() : Flow<List<Alarm>> {
        return withContext(Dispatchers.IO){ alarmDao.getAllFlow() }
    }

    override suspend fun getAlarm(id : Int) : Alarm {
        return withContext(Dispatchers.IO) { alarmDao.getAlarm(id) }
    }
}