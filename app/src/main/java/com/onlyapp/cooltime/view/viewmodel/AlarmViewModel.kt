package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.common.isExistMatchToday
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.model.AlarmModel
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.utils.getTodayStart
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {
    private val _alarmModelList: MutableLiveData<List<AlarmModel>> = MutableLiveData()
    val alarmModelList: LiveData<List<AlarmModel>>
        get() = _alarmModelList

    init {
        fetchData()
        startUpdatingData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            repository.getAllFlow().collect { alarmList ->
                val alarmModelList = alarmList.map { alarm ->
                    val remainTimeText = getRemainTime(alarm.day, alarm.time)
                    AlarmModel(alarm.id, alarm.name, alarm.day, alarm.time, remainTimeText)
                }
                _alarmModelList.value = alarmModelList
            }
        }
    }


    suspend fun insertAlarm(alarmModel: AlarmModel): Long {
        return repository.insert(
            Alarm(
                name = alarmModel.name,
                day = alarmModel.day,
                time = alarmModel.time
            )
        )
    }

    fun deleteAlarm(alarmModel: AlarmModel) = viewModelScope.launch {
        repository.delete(convertToAlarm(alarmModel))
    }


    fun updateAlarm(alarmModel: AlarmModel) = viewModelScope.launch {
        repository.update(convertToAlarm(alarmModel))
    }

    private fun convertToAlarm(alarmModel: AlarmModel): Alarm {
        return Alarm(alarmModel.id, alarmModel.name, alarmModel.day, alarmModel.time)
    }

    private fun startUpdatingData() {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                val updateList = alarmModelList.value?.map { item ->
                    item.copy(remainTime = getRemainTime(item.day, item.time))
                } ?: emptyList()

                _alarmModelList.postValue(updateList)
            }
        }
        timer.schedule(timerTask, 0, 30 * 1000)
    }

    private fun getRemainTime(day: Int, time: Int): String {
        val dayOfWeek = getTodayNow().get(Calendar.DAY_OF_WEEK)
        return when {
            !isExistMatchToday(dayOfWeek, day) -> "오늘은 알람이 울리지 않습니다."
            getTodayNow().timeInMillis < getTodayStart().apply {
                set(Calendar.HOUR_OF_DAY, time / 60)
                set(Calendar.MINUTE, time % 60)
            }.timeInMillis -> {
                val diffTime = time -
                        (getTodayNow().get(Calendar.HOUR_OF_DAY) * 60 + getTodayNow().get(Calendar.MINUTE))
                val hour = diffTime / 60
                val minute = diffTime % 60
                "알람 적용까지 ${hour}시간 ${minute}분 남았습니다."
            }

            else -> "오늘은 알람이 적용되지 않습니다."
        }
    }

}

