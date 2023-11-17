package com.onlyapp.cooltime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.MyApplication.Companion.waitCheck
import com.onlyapp.cooltime.databinding.FragmentActiveLockBinding
import com.onlyapp.cooltime.service.ActiveLockService
import com.onlyapp.cooltime.service.UseTimeService
import com.onlyapp.cooltime.view.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            Intent.ACTION_USER_PRESENT -> //잠금 해제 상황에서
                CoroutineScope(Dispatchers.Main).launch{
                    MyApplication.getInstance().getDataStore().increaseCnt()
                    MyApplication.getInstance().getDataStore().updateLockStatus()

                }

            Intent.ACTION_SCREEN_OFF -> //화면이 꺼진 상황에서
                CoroutineScope(Dispatchers.Main).launch{
                    val status = MyApplication.getInstance().getDataStore().lockStatus.first()
                    if(status){ //화면 잠금 해제되어 있는 상태일 때
                        MyApplication.getInstance().getDataStore().updateLatestUseTime(System.currentTimeMillis())
                        MyApplication.getInstance().getDataStore().updateLockStatus()
                        waitCheck = false
                    }

                }

            Intent.ACTION_DATE_CHANGED -> //날짜 변경될 때
                CoroutineScope(Dispatchers.Main).launch{
                    MyApplication.getInstance().getDataStore().onDateChanged()  //정보 초기화
                }

            Intent.ACTION_BOOT_COMPLETED -> //부팅됐다는 알람 왔을 때 처리할 로직
                context.startService(Intent(context, UseTimeService::class.java))

            "Reserved Alarm" -> {   //알람 상황에서
                val message = intent.getStringExtra("message")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                context.startActivity(Intent(context, MainActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }

        }
    }
}