package com.onlyapp.cooltime

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.onlyapp.cooltime.data.DataStoreModule
import com.onlyapp.cooltime.receiver.MyBroadcastReceiver

class MyApplication : Application() {
    private lateinit var dataStore : DataStoreModule

    override fun onCreate() {
        super.onCreate()
        val br =  MyBroadcastReceiver()
        myApplication = this
        dataStore = DataStoreModule(this)

        registerReceiver(br, IntentFilter().apply{  //브로드 캐스트 리시버 등록
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_DATE_CHANGED)
        }, )
    }
    fun getDataStore() = dataStore

    companion object{
        private lateinit var myApplication: MyApplication
        var waitCheck = true    //최소 시간 간격을 기다렸는지 확인하기 위한 변수

        fun getInstance() = myApplication
    }
}