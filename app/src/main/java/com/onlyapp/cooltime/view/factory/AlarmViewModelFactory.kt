package com.onlyapp.cooltime.view.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.AlarmRepositoryImpl
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel

class AlarmViewModelFactory(private val repository: AlarmRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}