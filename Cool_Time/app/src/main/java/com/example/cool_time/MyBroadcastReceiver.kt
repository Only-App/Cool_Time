package com.example.cool_time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action == Intent.ACTION_USER_PRESENT) { //잠금 해제 상황에서
            CoroutineScope(Dispatchers.Main).launch{
                MyApplication.getInstance().getDataStore().increaseCnt()
                MyApplication.getInstance().getDataStore().updateLockStatus()
            }
        }
        else if(intent!!.action == Intent.ACTION_SCREEN_OFF){   //화면이 꺼진 상황에서
            CoroutineScope(Dispatchers.Main).launch{
                val status = MyApplication.getInstance().getDataStore().lockStatus.first()
                if(status){ //화면 잠금 해제되어 있는 상태일 때
                    MyApplication.getInstance().getDataStore().updateLatestUseTime(System.currentTimeMillis())
                    MyApplication.getInstance().getDataStore().updateLockStatus()
                }

            }
        }
        else if(intent!!.action == Intent.ACTION_DATE_CHANGED){ //날짜 변경될 때
            Log.d("DateChanged", "TRUE")
            CoroutineScope(Dispatchers.Main).launch{
                MyApplication.getInstance().getDataStore().onDateChanged()  //정보 초기화
            }
        }
    }
}