package com.example.cool_time

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cool_time.model.Alarm

class AlarmRepository(private val alarmDao : AlarmDAO) {
    val allAlarm : LiveData<List<Alarm>> = alarmDao.getAll()


    suspend fun getAlarm(id : Int) {
        alarmDao.getAlarm(id)
    }
    suspend fun insert(alarm : Alarm){
        alarmDao.insertAlarm(alarm)
    }

    suspend fun delete(alarm : Alarm){
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun update(alarm: Alarm){
        val result = alarmDao.updateAlarm(alarm)
    }

}