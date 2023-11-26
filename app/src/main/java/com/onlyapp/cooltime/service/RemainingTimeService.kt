package com.onlyapp.cooltime.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.onlyapp.cooltime.R
import java.util.Timer
import java.util.TimerTask


class RemainingTimeService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var variable = intent.getIntExtra(getString(R.string.time), 0) // 초기 변수 값 설정
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                if (variable >= 0) {
                    val timeIntent = Intent(getString(R.string.remainingTime))
                    timeIntent.putExtra(getString(R.string.time), variable)
                    sendBroadcast(timeIntent)
                    variable--
                } else {
                    startService(Intent(this@RemainingTimeService, UseTimeService::class.java)) //잠금이 종료되면 사용 시간 체크 서비스 다시 시작
                    timer.cancel() // 변수가 0 이하일 때 타이머 종료
                    stopSelf()
                }
            }
        }
        timer.scheduleAtFixedRate(task, 0, 1000)
        // 백그라운드 서비스가 종료되지 않도록 설정
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


