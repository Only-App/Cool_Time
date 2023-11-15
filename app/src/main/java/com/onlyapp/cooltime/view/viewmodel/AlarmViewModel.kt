package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.entity.Alarm

class AlarmViewModel(private val repository : AlarmRepository) : ViewModel() {

    val alarmList : LiveData<List<Alarm>> = repository.allAlarm

    fun insertAlarm(alarm : Alarm) = repository.insert(alarm)

    fun deleteAlarm(alarm : Alarm) = repository.delete(alarm)


    fun updateAlarm(alarm : Alarm) = repository.update(alarm)
}

