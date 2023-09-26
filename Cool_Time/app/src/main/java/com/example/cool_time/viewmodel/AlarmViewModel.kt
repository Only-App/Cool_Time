package com.example.cool_time.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cool_time.AlarmRepository
import com.example.cool_time.model.Alarm
import kotlinx.coroutines.launch

class AlarmViewModel(private val repository : AlarmRepository) : ViewModel() {

    var alarm_list : LiveData<List<Alarm>>? = repository.allAlarm

    fun getAlarm(id : Int) = viewModelScope.launch{
        repository.getAlarm(id)
    }

    fun deleteAlarm(alarm : Alarm) = viewModelScope.launch{
        repository.delete(alarm)
    }

    fun updateAlarm(alarm : Alarm) = viewModelScope.launch{
        repository.update(alarm)
    }

}