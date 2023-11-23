package com.onlyapp.cooltime

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.onlyapp.cooltime.data.DataStoreModule

const val NATIVE_APP_KEY = "92226dfd8157cb8b6a20c3f518c39d06" // 안드로이드에서 KakaoSdk 호출 위한 네이티브 key 값

class MyApplication : Application() {
    private lateinit var dataStore: DataStoreModule

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        dataStore = DataStoreModule(this)

        //KakaoSdk 초기화
        KakaoSdk.init(this, NATIVE_APP_KEY)
    }

    fun getDataStore() = dataStore

    companion object {
        private lateinit var myApplication: MyApplication
        var waitCheck = true    //최소 시간 간격을 기다렸는지 확인하기 위한 변수

        fun getInstance() = myApplication
    }
}