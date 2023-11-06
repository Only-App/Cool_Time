package com.example.cool_time.background

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.cool_time.ui.ActiveLockActivity
import java.util.Timer
import java.util.TimerTask


class RemainingTime(): Service(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var variable = intent!!.getIntExtra("time", 0) // 초기 변수 값 설정
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                if (variable >= 0) {
                    val intent = Intent("my-custom-action")
                    intent.putExtra("myData", variable)
                    sendBroadcast(intent)
                    Log.d("tstst", "남은 시간 :" + variable.toString())
                    variable--
                } else {
                    timer.cancel() // 변수가 0 이하일 때 타이머 종료
                }
            }
        }
        timer.scheduleAtFixedRate(task, 0, 1000)
        // 백그라운드 서비스가 종료되지 않도록 설정
        return START_STICKY
    }
    override fun onDestroy() {
        // 서비스가 종료될 때 수행할 작업
        Log.d("tstst", "서비스 종료")
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


