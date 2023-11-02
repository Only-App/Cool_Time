package com.example.cool_time.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.cool_time.ui.ActiveLockActivity
import java.util.Timer
import java.util.TimerTask


class RemainingTime(): Service(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var variable = intent!!.getIntExtra("time", -1) // 초기 변수 값 설정

        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                if (variable >= 0) {
                    variable--
                    val intent = Intent("my-custom-action")
                    intent.putExtra("myData", variable)
                    sendBroadcast(intent)
                    println("변수 값: $variable")
                } else {
                    timer.cancel() // 변수가 0 이하일 때 타이머 종료
                }
            }
        }


        timer.scheduleAtFixedRate(task, 0, 1000)
        /*
        val startTime = sharedPreferences.getLong("start_time", 0L)
        val currentTime = System.currentTimeMillis()

        if (startTime == 0L) {
            // 앱이 처음 실행됐을 때
            sharedPreferences.edit().putLong("start_time", currentTime).apply()
        } else {
            // 앱이 이미 실행되었을 때
            val elapsedTime = currentTime - startTime
            val remainingTime = calculateRemainingTime(elapsedTime)
            // 데이터를 저장하거나 브로드캐스트를 통해 알림
        }


         */
        // 백그라운드 서비스가 종료되지 않도록 설정
        return START_STICKY
    }
/*
    private fun calculateRemainingTime(elapsedTime: Long): String {
        val remainingMillis = MAX_TIME - elapsedTime
        val remainingSeconds = remainingMillis / 1000
        val remainingMinutes = remainingSeconds / 60
        val remainingHours = remainingMinutes / 60

        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")

        return format.format(Date(remainingMillis))
    }

    companion object {
        private const val MAX_TIME = 24 * 60 * 60 * 1000 // 24 hours in milliseconds
    }


 */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}


