package com.example.cool_time.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateViewModel : ViewModel() {
    val date : MutableLiveData<Long> = MutableLiveData()



}