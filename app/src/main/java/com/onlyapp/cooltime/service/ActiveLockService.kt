package com.onlyapp.cooltime.service

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PixelFormat
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
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentActiveLockBinding
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.view.adapter.AppItem
import com.onlyapp.cooltime.view.adapter.LockScreenAdapter
import com.onlyapp.cooltime.view.itemdecoration.GridSpacingItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TreeMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ActiveLockService: Service() {
    companion object{
        const val POSSIBLE  = 0 //현재 사용 가능 상태
        const val EXCEED = 1    //총 사용 시간을 초과한 상태
        const val LOCK_DURATION = 2 //잠금 시간이 적용된 상태
        const val WAIT = 3  //최소 사용 시간 간격으로 사용할 수 없는 상태

    }

    private val appItems = mutableListOf<AppItem>() // 예외 앱의 이름과, 아이콘 데이터 리스트
    private val exceptionAppList = arrayListOf<String>() // 예외 앱 이름 리스트
    private var flag = false // 오버레이(잠금화면)이 떠있는지 체크하는 값
    //val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!
    private lateinit var binding:FragmentActiveLockBinding// 바인딩
    private lateinit var view:View // 오버레이 위에 띄울 뷰
    private lateinit var windowManager : WindowManager //오버레이 띄우기 위한 윈도우 매니저
    private lateinit var params:LayoutParams
    private val executor = Executors.newSingleThreadScheduledExecutor() // 타이머 같은거
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val  receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "remainingTime") {
                val receivedData = intent.getIntExtra("time", 0)
                // 데이터를 처리
                // receivedData를 사용
                // 예: TextView에 표시
                val useTime = "${receivedData / 3600}시간 ${receivedData % 3600 / 60}분 ${receivedData % 60}초 남았습니다"
                binding.lockUseTime.text = useTime
                if(receivedData == 0){
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var togle = false
        binding = FragmentActiveLockBinding.inflate(LayoutInflater.from(this))
        view = binding.root
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        params = LayoutParams( //오버레이 관련 옵션
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY, // 항상 위에 유지
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        binding.scroll.visibility=View.GONE
        binding.lockTypeComment.visibility=View.VISIBLE
        binding.lockUseTime.visibility=View.VISIBLE


        val loadIconJob = CoroutineScope(Dispatchers.IO).async{
            val packageManager = this@ActiveLockService.applicationContext.packageManager
            val exceptAppDao = checkNotNull(UserDatabase.getInstance(this@ActiveLockService)).exceptAppsDao()
            val exceptAppRepository = ExceptAppRepository(exceptAppDao)
            val exceptAppList = exceptAppRepository.getAllApps()

            for(exceptApp in exceptAppList){
                if(!exceptApp.checked) continue //예외앱만 리스트에 추가
                val packageName = exceptApp.packageName
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val appIcon = appInfo.loadIcon(packageManager) ?: R.drawable.baseline_android_24.toDrawable()
                val appItem = AppItem(packageName, appIcon)
                appItems.add(appItem)
                exceptionAppList.add(packageName)
            }

            val adapter = LockScreenAdapter(this@ActiveLockService, appItems, packageManager)
            binding.appList.adapter = adapter
            binding.appList.layoutManager  = GridLayoutManager(this@ActiveLockService, 3, GridLayoutManager.VERTICAL, false)
            binding.appList.addItemDecoration(GridSpacingItemDecoration(spanCount = 3, spacing = 10))

        }

        binding.menu.setOnClickListener{
            if(togle){
                binding.scroll.visibility=View.GONE
                binding.lockTypeComment.visibility=View.VISIBLE
                binding.lockUseTime.visibility=View.VISIBLE
            }
            else{
                binding.scroll.visibility=View.VISIBLE
                binding.lockTypeComment.visibility=View.GONE
                binding.lockUseTime.visibility=View.GONE

            }
            togle = !togle
        }

        binding.call.setOnClickListener{
            val sendIntent = Intent(Intent.ACTION_DIAL)
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this, sendIntent, null)
        }

        binding.message.setOnClickListener{
            val sendIntent = Intent(Intent.ACTION_MAIN)
            sendIntent.addCategory(Intent.CATEGORY_DEFAULT)
            sendIntent.setPackage("com.samsung.android.messaging")
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this, sendIntent, null)
        }

        val time = intent.getIntExtra("time", 0)
        val serviceIntent = Intent(this, RemainingTimeService::class.java)
        serviceIntent.putExtra("time", time)
        startService(serviceIntent)

        val filter = IntentFilter("remainingTime")
        registerReceiver(receiver, filter)



        params.gravity = Gravity.START or Gravity.TOP

        flag = true
        windowManager.addView(view, params) // 오버레이를 작동시키면서 flag를 true로 설정해서 overlay가 켜졌음을 알 수 있도록 함

        val lockType = intent.getIntExtra("lockType", -1)
        val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!


        val runnable = Runnable {
            // UI 업데이트 코드를 여기에 작성

            handler.post {  //날짜, 시간 출력
                val month = getTodayNow().get(Calendar.MONTH)
                val date = getTodayNow().get(Calendar.DATE)
                val day = when (getTodayNow().get(Calendar.DAY_OF_WEEK)) {
                    1 -> "일"; 2 -> "월"; 3 -> "화"; 4 -> "수"; 5 -> "목"; 6 -> "금"; else -> "토"
                }


                val hour = getTodayNow().get(Calendar.HOUR_OF_DAY)
                val minute = getTodayNow().get(Calendar.MINUTE)
                val lockToday = "${month}월 ${date}일 ${day}요일"
                binding.lockToday.text = lockToday

                if (hour >= 12){
                    val lockTime = "오후 ${hour - 12}시간 ${minute}분"
                    binding.lockTime.text = lockTime
                }
                else{
                    val lockTime = "오전 ${hour}시간 ${minute}분"
                    binding.lockTime.text = lockTime
                }

                //각 잠금 타입에 맞게 텍스트 출력

                when (lockType){
                    WAIT -> binding.lockTypeComment.text = "재사용 가능까지"
                    EXCEED -> binding.lockTypeComment.text = "오늘은 더 이상 사용할 수 없습니다"
                    LOCK_DURATION -> binding.lockTypeComment.text = "잠금이 적용되었습니다"
                }

            }

            val mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = mUsageStatsManager.queryUsageStats( // 가장 최근 기록 불러오기 위해 실행한다고 이해하면 됨
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
            )

            stats?.let { //사실 Null이면 뭐 그동안 상태 변화 없었다는거니까 괜찮을 것
                val mySortedMap = TreeMap<Long, UsageStats>()
                for (usageStats in stats) {
                    mySortedMap[usageStats.lastTimeUsed] = usageStats
                }
                if (mySortedMap.isNotEmpty()) {
                    val topPackageName = mySortedMap[mySortedMap.lastKey()]?.packageName
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    packageManager.resolveActivity(callIntent, PackageManager.MATCH_DEFAULT_ONLY)?.let {
                        val callPackageName = it.activityInfo.packageName
                        if (callPackageName != topPackageName && topPackageName != "com.samsung.android.messaging" && !exceptionAppList.contains(topPackageName)) { // 예외 리스트 앱에 포함되어 있지 않은 앱이 현재 죄상위(오버레이 제외) 레이아웃에서 실행되고 있다면
                            if (!flag) { // 오버레이가 작동하고 있지 않다면
                                handler.post { // Ui 작업 관련이니까 handler 실행
                                    flag = true
                                    windowManager.addView(view, params) // 오버레이 실행
                                    // 근데 밀리 초 단위로 실행시키다 보니까 오버레이 실행 코드와 변수 변경하는 코드를 전부 실행하기 전에 Context Switch가 일어나는건지(확실 X)
                                    // 이미 오버레이 있는데 flag 값이 변경 되기 전이었는지 여기 조건문으로 들어와 다시 띄우려고 하다가 충돌이 일어나는 현상 있는 것 같음, 확실하진 않고 만약 맞다면 신기
                                    // 그래서 flag를 먼저 우선 변경해주고, 만들기로 해서 중복 만들기 방지
                                }
                            }
                        } else {
                            if (flag) { // 오버레이 있을 때만 오버레이 해제, 없는데 없애려고 하거나, 있는데 또 만드려고 하면 충돌 생김
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

        CoroutineScope(Dispatchers.Main).launch{
            loadIconJob.await()
            executor.scheduleWithFixedDelay(runnable, 0, 300, TimeUnit.MILLISECONDS) // 30밀리초마다 스케쥴 돌아가도록 함
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown() // 타이머 종료
        if(flag){ // 만약 잠금 시간 다 끝나서 서비스 종료하려고 할 때 오버레이가 작동중이었다면 해제하고 종료
            windowManager.removeView(view)
        }
        unregisterReceiver(receiver)
    }
}
