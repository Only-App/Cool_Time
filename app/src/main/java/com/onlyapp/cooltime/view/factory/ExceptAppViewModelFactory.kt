package com.onlyapp.cooltime.view.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.view.viewmodel.ExceptAppViewModel
import java.lang.IllegalArgumentException

class ExceptAppViewModelFactory(private val repository: ExceptAppRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExceptAppViewModel::class.java)){
            return ExceptAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}