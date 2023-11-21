package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.launch

class AlarmViewModel(private val repository : AlarmRepository) : ViewModel() {

    val alarmList : LiveData<List<Alarm>> = repository.allAlarm

    suspend fun insertAlarm(alarm : Alarm)  : Long {
        return repository.insert(alarm)
    }

    fun deleteAlarm(alarm : Alarm) = viewModelScope.launch {repository.delete(alarm) }


    fun updateAlarm(alarm : Alarm) = viewModelScope.launch { repository.update(alarm) }
}

