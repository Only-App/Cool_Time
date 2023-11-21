package com.onlyapp.cooltime

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.onlyapp.cooltime.data.DataStoreModule
import com.onlyapp.cooltime.receiver.MyBroadcastReceiver

const val NATIVE_APP_KEY = "92226dfd8157cb8b6a20c3f518c39d06" // 안드로이드에서 KakaoSdk 호출 위한 네이티브 key 값
class MyApplication : Application() {
    private lateinit var dataStore : DataStoreModule

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        dataStore = DataStoreModule(this)

        //KakaoSdk 초기화
        KakaoSdk.init(this, NATIVE_APP_KEY)
        //디버그용 키 해시 값을 받기 위한 코드 => log에 찍힌 값이 디버그 키
        //디버그 키는 컴퓨터마다 다르다! 그러므로 개발 팀원 모두의 디버그 키를 각각 등록해줘야 함
        // 그래서 한번 실행해보시구 찍힌 값 알려주세용!
        val keyHash = Utility.getKeyHash(this)
        Log.d("keyHash", keyHash) //
    }
    fun getDataStore() = dataStore

    companion object{
        private lateinit var myApplication: MyApplication
        var waitCheck = true    //최소 시간 간격을 기다렸는지 확인하기 위한 변수

        fun getInstance() = myApplication
    }
}