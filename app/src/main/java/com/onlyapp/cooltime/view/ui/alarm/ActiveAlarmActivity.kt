package com.onlyapp.cooltime.view.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.ActivityActiveAlarmBinding
import com.onlyapp.cooltime.utils.getTodayNow
import java.util.Calendar

class ActiveAlarmActivity : AppCompatActivity() {
    private var _binding : ActivityActiveAlarmBinding? = null
    private val binding : ActivityActiveAlarmBinding
        get() = _binding!!

    private var mediaPlayer : MediaPlayer? = null
    private var countDownTimer : CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityActiveAlarmBinding.inflate(layoutInflater)

        // 안드12 이상에서 잠금화면 위로 액티비티 띄우기 & 화면 켜기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).apply {
                requestDismissKeyguard(this@ActiveAlarmActivity, null)
            }
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        //알람 내용 receive
        val message = intent.getStringExtra("message")
        binding.alarmTitle.text = message

        val month = getTodayNow().get(Calendar.MONTH)
        val date = getTodayNow().get(Calendar.DATE)
        val day = when (getTodayNow().get(Calendar.DAY_OF_WEEK)) {
            1 -> "일"; 2 -> "월"; 3 -> "화"; 4 -> "수"; 5 -> "목"; 6 -> "금"; else -> "토"
        }


        val hour = getTodayNow().get(Calendar.HOUR_OF_DAY)
        val minute = getTodayNow().get(Calendar.MINUTE)
        val lockToday = "${month}월 ${date}일 ${day}요일"
        binding.alarmToday.text = lockToday

        if (hour >= 12){
            val lockTime = "오후 ${hour - 12}시간 ${minute}분"
            binding.alarmTime.text = lockTime
        }
        else{
            val lockTime = "오전 ${hour}시간 ${minute}분"
            binding.alarmTime.text = lockTime
        }

        //취소 버튼 클릭시 알람 재생 멈추고 액티비티 종료
        binding.closeButton.setOnClickListener{
            stopAlarmSound()
            finish()
        }


        startAlarmSound()   //알람음 재생
        Log.d("mediaPlayer", mediaPlayer?.isPlaying.toString())


        //1분 동안 알람 재생 후 종료하도록 타이머설정
        countDownTimer = object: CountDownTimer(60 * 1000, 1000){
            override fun onTick(p0: Long) {
            }
            override fun onFinish() {
                if(mediaPlayer?.isPlaying == true){
                    stopAlarmSound()
                    finish()
                }
            }
        }.start()



        setContentView(binding.root)
    }

    private fun startAlarmSound(){  //알람음 재생
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        if(mediaPlayer != null){
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(100f, 100f)
            mediaPlayer?.start()
        }
    }

    private fun stopAlarmSound(){   //알람음 재생 종료
        if(mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        if(countDownTimer != null){
            countDownTimer?.cancel()
        }
    }
}