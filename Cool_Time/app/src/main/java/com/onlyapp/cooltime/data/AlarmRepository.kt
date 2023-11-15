package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.data.entity.Alarm

class AlarmRepository(private val alarmDao : AlarmDAO) {
    val allAlarm : LiveData<List<Alarm>> = alarmDao.getAll()

    fun insert(alarm : Alarm){
        alarmDao.insertAlarm(alarm)
    }

    fun delete(alarm : Alarm){
        alarmDao.deleteAlarm(alarm)
    }

    fun update(alarm: Alarm){
        val result = alarmDao.updateAlarm(alarm)
    }

}