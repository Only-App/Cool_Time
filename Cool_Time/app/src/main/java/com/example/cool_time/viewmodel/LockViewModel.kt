package com.example.cool_time.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cool_time.LockRepository
import com.example.cool_time.model.PhoneLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class LockViewModel (private val repository: LockRepository) : ViewModel(){
    var lock_list : LiveData<List<PhoneLock>> = repository.allLock

    fun insertLock(lock : PhoneLock) = viewModelScope.launch(Dispatchers.IO){
        repository.insertLock(lock)
    }

    fun getLock(id : Int) = viewModelScope.launch(Dispatchers.IO){
        repository.getLock(id)
    }

    fun deleteLock(lock : PhoneLock) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteLock(lock)
    }

    fun updateLock(lock : PhoneLock) = viewModelScope.launch(Dispatchers.IO){
        repository.updateLock(lock)
    }

}

class LockViewModelFactory(private val repository: LockRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LockViewModel::class.java)){
            return LockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}