package com.example.cool_time.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cool_time.data.ExceptAppRepository
import com.example.cool_time.model.ExceptApp
import java.lang.IllegalArgumentException

class ExceptAppViewModel(private val repository: ExceptAppRepository) : ViewModel() {
    val exceptApp : LiveData<List<ExceptApp>> = repository.allApps

    fun insertApp(app : ExceptApp) {
        repository.insertApp(app)
    }

    fun deleteApp(packageName : String){
        repository.deleteApp(packageName)
    }
    fun getApp(packageName : String) : ExceptApp{
        return repository.getApp(packageName)
    }

    fun updateApp(app : ExceptApp){
        repository.updateApp(app)
    }
}

class ExceptAppViewModelFactory(private val repository: ExceptAppRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExceptAppViewModel::class.java)){
            return ExceptAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}