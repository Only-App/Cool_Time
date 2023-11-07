package com.example.cool_time

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cool_time.receiver.MyBroadcastReceiver

class MyApplication : Application() {
    private lateinit var dataStore : DataStoreModule
    companion object{
        private lateinit var myApplication: MyApplication
        fun getInstance() = myApplication
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        val br =  MyBroadcastReceiver()
        myApplication = this
        dataStore = DataStoreModule(this)

        registerReceiver(br, IntentFilter().apply{  //브로드 캐스트 리시버 등록
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_DATE_CHANGED)
        })
    }



    fun getDataStore() = dataStore
}