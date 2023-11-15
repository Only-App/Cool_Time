package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.entity.ExceptApp

class ExceptAppViewModel(private val repository: ExceptAppRepository) : ViewModel() {
    val exceptApp : LiveData<List<ExceptApp>> = repository.allApps

    fun insertApp(app : ExceptApp) {
        repository.insertApp(app)
    }

    fun deleteApp(packageName : String){
        repository.deleteApp(packageName)
    }
    fun getApp(packageName : String) : ExceptApp {
        return repository.getApp(packageName)
    }

    fun updateApp(app : ExceptApp){
        repository.updateApp(app)
    }
}

