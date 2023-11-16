package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.launch

class LockViewModel (private val repository: LockRepository) : ViewModel(){
    val lockList : LiveData<List<PhoneLock>> = repository.allLock

    fun insertLock(lock : PhoneLock) = viewModelScope.launch{ repository.insertLock(lock) }

    fun deleteLock(lock : PhoneLock) = viewModelScope.launch{ repository.deleteLock(lock) }

    fun updateLock(lock : PhoneLock) = viewModelScope.launch{ repository.updateLock(lock) }


}

