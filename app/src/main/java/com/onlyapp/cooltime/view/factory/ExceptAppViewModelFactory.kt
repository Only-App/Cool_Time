package com.onlyapp.cooltime.view.factory

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.view.viewmodel.ExceptAppViewModel

class ExceptAppViewModelFactory(private val repository: ExceptAppRepository, private val getAppInfo : (ExceptApp) -> Pair<String, Drawable>) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExceptAppViewModel::class.java)){
            return ExceptAppViewModel(repository, getAppInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}