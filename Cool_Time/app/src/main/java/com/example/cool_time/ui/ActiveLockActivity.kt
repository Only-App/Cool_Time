package com.example.cool_time.ui

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cool_time.R
import com.example.cool_time.background.RemainingTime
import com.example.cool_time.databinding.FragmentActiveLockBinding
import com.example.cool_time.viewmodel.AppItem
import com.example.cool_time.viewmodel.GridSpacingItemDecoration
import com.example.cool_time.viewmodel.LockScreenAdapter
import java.util.TreeMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ActiveLockActivity(): Service() {
    private val datas = mutableListOf<AppItem>()
    private val app_list = ArrayList<String>() // 예외 앱 리스트
    private var flag = false // 오버레이(잠금화면)이 떠있는지 체크하는 값
    //val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!
    private lateinit var binding:FragmentActiveLockBinding// 바인딩
    private lateinit var view:View // 오버레이 위에 띄울 뷰
    private lateinit var windowManager : WindowManager //오버레이 띄우기 위한 윈도우 매니저
    private lateinit var params:LayoutParams

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private val  receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "my-custom-action") {
                val receivedData = intent.getIntExtra("myData", 0)
                // 데이터를 처리
                if (receivedData != null) {
                    // receivedData를 사용
                    // 예: TextView에 표시
                    if(receivedData == 0){
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var togle = false
        binding = FragmentActiveLockBinding.inflate(LayoutInflater.from(this))
        view = binding.root
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams( //오버레이 관련 옵션
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 항상 위에 유지
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        binding.scroll.visibility=View.GONE
        binding.lastUseComment.visibility=View.VISIBLE
        binding.lastUseTime.visibility=View.VISIBLE

        val packageManager = this.applicationContext.packageManager
        val packages:List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
        for(info: PackageInfo in packages){
            if(packageManager.getLaunchIntentForPackage(info.packageName) != null && info.applicationInfo.name != null) {
                val iticon: Drawable = info.applicationInfo.loadIcon(packageManager) ?: R.drawable.baseline_android_24.toDrawable()
                val it = AppItem(info.packageName, iticon)
                datas.add(it)
                app_list.add(info.packageName)
            }
        }

        val adapter = LockScreenAdapter(this, datas, packageManager)
        binding.appList.adapter = adapter
        binding.appList.layoutManager  = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
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
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage("com.samsung.android.messaging");
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this!!, intent, null)
        }

        val time = intent!!.getIntExtra("time", 0)
        val serviceIntent = Intent(this, RemainingTime::class.java)
        serviceIntent.putExtra("time", time)
        startService(serviceIntent)

        val filter = IntentFilter("my-custom-action")
        registerReceiver(receiver, filter)


        params.gravity = Gravity.START or Gravity.TOP

        flag = true
        windowManager.addView(view, params) // 오버레이를 작동시키면서 flag를 true로 설정해서 overlay가 켜졌음을 알 수 있도록 함


        val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!
        val executor = Executors.newSingleThreadScheduledExecutor() // 타이머 같은거
        val runnable = Runnable {
            // UI 업데이트 코드를 여기에 작성
            val mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = mUsageStatsManager.queryUsageStats( // 가장 최근 기록 불러오기 위해 실행한다고 이해하면 됨
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
            )

            var topPackageName: String? = null

            if (stats != null) { //사실 Null이면 뭐 그동안 상태 변화 없었다는거니까 괜찮을 것
                val mySortedMap = TreeMap<Long, UsageStats>()
                for (usageStats in stats) {
                    mySortedMap[usageStats.lastTimeUsed] = usageStats
                }
                if (mySortedMap.isNotEmpty()) {
                    topPackageName = mySortedMap[mySortedMap.lastKey()]?.packageName
                    if (topPackageName != "com.example.cool_time") { // 일단 그냥 테스트 겸 자사 앱은 로직에서 예외처리 해놨음
                        if (!app_list!!.contains(topPackageName)) { // 예외 리스트 앱에 포함되어 있지 않은 앱이 현재 죄상위(오버레이 제외) 레이아웃에서 실행되고 있다면
                            if(!flag) { // 오버레이가 작동하고 있지 않다면
                                handler.post { // Ui 작업 관련이니까 handler 실행
                                    flag = true
                                    windowManager.addView(view, params) // 오버레이 실행
                                    // 근데 밀리 초 단위로 실행시키다 보니까 오버레이 실행 코드와 변수 변경하는 코드를 전부 실행하기 전에 Context Switch가 일어나는건지(확실 X)
                                    // 이미 오버레이 있는데 flag 값이 변경 되기 전이었는지 여기 조건문으로 들어와 다시 띄우려고 하다가 충돌이 일어나는 현상 있는 것 같음, 확실하진 않고 만약 맞다면 신기
                                    // 그래서 flag를 먼저 우선 변경해주고, 만들기로 해서 중복 만들기 방지
                                }
                            }
                        } else {
                            if(flag) { // 오버레이 있을 때만 오버레이 해제, 없는데 없애려고 하거나, 있는데 또 만드려고 하면 충돌 생김
                                handler.post {
                                    windowManager.removeView(view)
                                    flag = false
                                }
                            }
                        }
                    }
                }
            }
        }

        executor.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.MILLISECONDS) // 30밀리초마다 스케쥴 돌아가도록 함
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        if(flag){ // 만약 잠금 시간 다 끝나서 서비스 종료하려고 할 때 오버레이가 작동중이었다면 해제하고 종료
            windowManager.removeView(view)
        }
    }
}
