package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.model.PhoneLockModel
import kotlinx.coroutines.launch

class LockViewModel(private val repository: LockRepository) : ViewModel() {
    private var _lockModelList: MutableLiveData<List<PhoneLockModel>> = MutableLiveData()
    val lockModelList get() = _lockModelList

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            repository.getAllFlow().collect { lockList ->
                val lockModelList = lockList.map { lock ->
                    convertToLockModel(lock)
                }
                _lockModelList.value = lockModelList
            }
        }
    }

    fun insertLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.insertLock(convertToLock(lockModel)) }

    fun deleteLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.deleteLock(convertToLock(lockModel)) }

    fun updateLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.updateLock(convertToLock(lockModel)) }


    private fun convertToLock(lockModel: PhoneLockModel): PhoneLock {
        return PhoneLock(
            lockModel.id,
            lockModel.totalTime,
            lockModel.minTime,
            lockModel.lockOn,
            lockModel.lockOff,
            lockModel.lockDay,
            lockModel.startDate,
            lockModel.endDate
        )
    }

    private fun convertToLockModel(lock: PhoneLock): PhoneLockModel {
        return PhoneLockModel(
            lock.id,
            lock.totalTime,
            lock.minTime,
            lock.lockOn,
            lock.lockOff,
            lock.lockDay,
            lock.startDate,
            lock.endDate
        )
    }
}

