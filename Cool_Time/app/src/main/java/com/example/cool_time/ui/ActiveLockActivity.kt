package com.example.cool_time.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cool_time.R
import com.example.cool_time.background.RemainingTime
import com.example.cool_time.databinding.FragmentActiveLockBinding
import com.example.cool_time.viewmodel.GridSpacingItemDecoration
import com.example.cool_time.viewmodel.AppItem
import com.example.cool_time.viewmodel.LockScreenAdapter
import java.util.Timer
import java.util.TimerTask


class ActiveLockActivity(): AppCompatActivity(){
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val binding = FragmentActiveLockBinding.inflate(layoutInflater)
            if (intent != null && intent.action == "my-custom-action") {
                val receivedData = intent.getIntExtra("myData", -1)

                // 데이터를 처리
                if (receivedData != null) {
                    // receivedData를 사용
                    // 예: TextView에 표시
                    Log.d("tstst", receivedData.toString())
                    if(receivedData == 0){
                        finish()
                    }

                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val time = intent.getIntExtra("time", -1)
        val binding = FragmentActiveLockBinding.inflate(layoutInflater)
        //val _binding = FragmentPhoneLockBinding.inflate(layoutInflater)
        var togle = false;
        binding.scroll.visibility=View.GONE
        binding.lastUseComment.visibility=View.VISIBLE
        binding.lastUseTime.visibility=View.VISIBLE

        val datas = mutableListOf<AppItem>()
        val packageManager = this.packageManager
        val packages:List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
        var count = 0
        for(info: PackageInfo in packages){
            if(packageManager.getLaunchIntentForPackage(info.packageName) != null && info.applicationInfo.name != null) {
                val iticon: Drawable = info.applicationInfo.loadIcon(packageManager)
                val it = AppItem(info.packageName, iticon)
                datas.add(it)
                count += 1
            }
        }

        val adapter = LockScreenAdapter(this!!, datas, packageManager)
        binding.appList.adapter = adapter
        binding.appList.layoutManager  = GridLayoutManager(this!!, 3, GridLayoutManager.VERTICAL, false,)
        binding.appList.addItemDecoration(GridSpacingItemDecoration(spanCount = 3, spacing = 10))


        binding.menu.setOnClickListener{
            if(togle){
                binding.scroll.visibility=View.GONE
                binding.lastUseComment.visibility=View.VISIBLE
                binding.lastUseTime.visibility=View.VISIBLE
            }
            else{
                binding.scroll.visibility=View.VISIBLE
                binding.lastUseComment.visibility=View.GONE
                binding.lastUseTime.visibility=View.GONE


            }
            togle = !togle
        }

        binding.call.setOnClickListener{
            val intent = Intent(Intent.ACTION_DIAL)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this!!, intent, null)
        }

        binding.message.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = "vnd.android-dir/mms-sms"
            ContextCompat.startActivity(this!!, intent, null)
        }
        val serviceIntent = Intent(this, RemainingTime::class.java)
        serviceIntent.putExtra("time", time)
        startService(serviceIntent)
        val filter = IntentFilter("my-custom-action")
        registerReceiver(receiver, filter)
        setContentView(binding.root)


//        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}