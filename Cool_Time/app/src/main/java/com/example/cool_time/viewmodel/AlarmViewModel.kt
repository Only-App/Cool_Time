package com.example.cool_time.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import com.example.cool_time.AlarmRepository
import com.example.cool_time.model.Alarm
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

class AlarmViewModelFactory(private val repository: AlarmRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AlarmViewModel::class.java)){
            return AlarmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}