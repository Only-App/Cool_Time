package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.model.AlarmModel
import kotlinx.coroutines.launch

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {
    private val _alarmModelList : MutableLiveData<List<AlarmModel>> = MutableLiveData()
    val alarmModelList : LiveData<List<AlarmModel>>
        get() = _alarmModelList

    init{
        fetchData()
    }
    private fun fetchData(){
        viewModelScope.launch{
            repository.getAllFlow().collect{
                alarmList ->
                val alarmModelList = alarmList.map{
                    alarm ->
                    AlarmModel(alarm.id, alarm.name, alarm.day, alarm.time)
                }
                _alarmModelList.value = alarmModelList
            }
        }
    }


    suspend fun insertAlarm(alarmModel: AlarmModel): Long {
        return repository.insert(Alarm(name = alarmModel.name, day = alarmModel.day, time = alarmModel.time))
    }

    fun deleteAlarm(alarmModel: AlarmModel) = viewModelScope.launch {
        repository.delete(convertToAlarm(alarmModel)) }


    fun updateAlarm(alarmModel: AlarmModel) = viewModelScope.launch {
        repository.update(convertToAlarm(alarmModel)) }

    private fun convertToAlarm(alarmModel : AlarmModel) : Alarm {
        return Alarm(alarmModel.id, alarmModel.name, alarmModel.day, alarmModel.time)
    }
}

