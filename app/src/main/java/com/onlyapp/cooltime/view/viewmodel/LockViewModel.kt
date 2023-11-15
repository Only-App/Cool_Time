package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.entity.PhoneLock

class LockViewModel (private val repository: LockRepository) : ViewModel(){
    val lockList : LiveData<List<PhoneLock>> = repository.allLock

    fun insertLock(lock : PhoneLock) = repository.insertLock(lock)


    fun getLock(id : Int) = repository.getLock(id)


    fun deleteLock(lock : PhoneLock) = repository.deleteLock(lock)


    fun updateLock(lock : PhoneLock) = repository.updateLock(lock)


}

