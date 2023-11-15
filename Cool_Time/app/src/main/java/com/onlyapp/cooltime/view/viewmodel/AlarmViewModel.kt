package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.model.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class AlarmViewModel(private val repository : AlarmRepository) : ViewModel() {

    var alarm_list : LiveData<List<Alarm>> = repository.allAlarm

    fun insertAlarm(alarm : Alarm) = viewModelScope.launch(Dispatchers.IO){
        repository.insert(alarm)

    }
    fun getAlarm(id : Int) = viewModelScope.launch(Dispatchers.IO){
        repository.getAlarm(id)
    }

    fun deleteAlarm(alarm : Alarm) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(alarm)
    }

    fun updateAlarm(alarm : Alarm) = viewModelScope.launch(Dispatchers.IO){
        repository.update(alarm)
    }


}

