package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.repository.LockRepository
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
            repository.getAllFlow().collect { lockModelList ->
                _lockModelList.value = lockModelList
            }
        }
    }

    fun insertLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.insertLock(lockModel) }

    fun deleteLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.deleteLock(lockModel) }

    fun updateLock(lockModel: PhoneLockModel) =
        viewModelScope.launch { repository.updateLock(lockModel) }

}

