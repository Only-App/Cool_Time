package com.onlyapp.cooltime.view.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlyapp.cooltime.repository.LockRepository
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import java.lang.IllegalArgumentException

class LockViewModelFactory(private val repository: LockRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LockViewModel::class.java)){
            return LockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}