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
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.GridLayoutManager
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.repository.ExceptAppRepositoryImpl
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

class ActiveLockService : Service() {

    private val appItems = mutableListOf<AppItem>() // 예외 앱의 이름과, 아이콘 데이터 리스트
    private val exceptionAppList = arrayListOf<String>() // 예외 앱 이름 리스트
    private var flag = false // 오버레이(잠금화면)이 떠있는지 체크하는 값

    //private val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!
    private lateinit var binding: FragmentActiveLockBinding// 바인딩
    private lateinit var view: View // 오버레이 위에 띄울 뷰
    private lateinit var windowManager: WindowManager //오버레이 띄우기 위한 윈도우 매니저
    private lateinit var params: LayoutParams
    private val executor = Executors.newSingleThreadScheduledExecutor() // 타이머 같은거
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.remainingTime) {
                val receivedData = intent.getIntExtra(Constants.time, 0)
                // 데이터를 처리
                // receivedData를 사용
                // 예: TextView에 표시
                val useTime = getString(R.string.remain_time, String.format("%02d", receivedData / 3600), String.format("%02d", receivedData % 3600 / 60), String.format("%02d", receivedData % 60))
                //"${receivedData / 3600}시간 ${receivedData % 3600 / 60}분 ${receivedData % 60}초 남았습니다"
                binding.lockUseTime.text = useTime
                if (receivedData == 0) {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var toggle = false
        val callPackageName = checkNotNull(returnCallPackageName())
        val messagePackageName = checkNotNull(returnMessagePackageName()) { getString(R.string.no_default_message) }
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

        binding.apply {
            scroll.visibility = View.GONE
            lockTypeComment.visibility = View.VISIBLE
            lockUseTime.visibility = View.VISIBLE
        }

        val loadIconJob = CoroutineScope(Dispatchers.IO).async {
            val packageManager = this@ActiveLockService.applicationContext.packageManager
            val exceptAppDao =
                checkNotNull(UserDatabase.getInstance(this@ActiveLockService)).exceptAppsDao()
            val exceptAppRepository = ExceptAppRepositoryImpl(exceptAppDao)

            Log.d("Before collect", "enter")

            CoroutineScope(Dispatchers.IO).launch {
                exceptAppRepository.allApps().collect { exceptAppList ->
                    for (exceptApp in exceptAppList) {
                        if (!exceptApp.checked) continue //예외앱만 리스트에 추가

                        val packageName = exceptApp.packageName

                        val appInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getApplicationInfo(
                                    packageName,
                                    PackageManager.ApplicationInfoFlags.of(0)
                                )
                            } else {
                                packageManager.getApplicationInfo(
                                    packageName,
                                    PackageManager.GET_META_DATA
                                )
                            }
                        val appIcon =
                            appInfo.loadIcon(packageManager)
                                ?: R.drawable.baseline_android_24.toDrawable()
                        val appItem = AppItem(packageName, appIcon)
                        appItems.add(appItem)
                        exceptionAppList.add(packageName)
                    }
                }
            }

            Log.d("After collect", "enter")

            fun startApp(packageName: String) {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(this@ActiveLockService, it, null)
                }
            }

            val adapter = LockScreenAdapter(appItems) { packageName -> startApp(packageName) }
            binding.appList.adapter = adapter
            binding.appList.layoutManager =
                GridLayoutManager(this@ActiveLockService, 3, GridLayoutManager.VERTICAL, false)
            binding.appList.addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 3, spacing = 10
                )
            )
        }

        binding.menu.setOnClickListener {
            if (toggle) {
                binding.scroll.visibility = View.GONE
                binding.lockTypeComment.visibility = View.VISIBLE
                binding.lockUseTime.visibility = View.VISIBLE
            } else {
                binding.scroll.visibility = View.VISIBLE
                binding.lockTypeComment.visibility = View.GONE
                binding.lockUseTime.visibility = View.GONE
            }
            toggle = !toggle
        }

        binding.call.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_DIAL)
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this, sendIntent, null)
        }

        binding.message.setOnClickListener {
            val sendIntent = packageManager.getLaunchIntentForPackage(messagePackageName)
            checkNotNull(sendIntent) { R.string.message_intent_error }
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(this, sendIntent, null)
        }

        val time = intent.getIntExtra(Constants.time, 0)
        val serviceIntent = Intent(this, RemainingTimeService::class.java)
        serviceIntent.putExtra(Constants.time, time)
        startService(serviceIntent)

        val filter = IntentFilter(Constants.remainingTime)
        registerReceiver(receiver, filter)

        params.gravity = Gravity.START or Gravity.TOP
        flag = true
        windowManager.addView(view, params) // 오버레이를 작동시키면서 flag를 true로 설정해서 overlay가 켜졌음을 알 수 있도록 함

        val lockType = intent.getIntExtra(Constants.lockType, -1)
        val handler = Handler(Looper.getMainLooper()) // runnable 내에서 ui 관련 처리할 때 이거 통해서 해야 함!

        Log.d("lockType", lockType.toString())


        val runnable = Runnable {
            // UI 업데이트 코드를 여기에 작성
            Log.d("runnable", "isRunning")
            handler.post {  //날짜, 시간 출력
                Log.d("handlerPost", "isGoing")
                val month = getTodayNow().get(Calendar.MONTH)
                val date = getTodayNow().get(Calendar.DATE)
                val day = when (getTodayNow().get(Calendar.DAY_OF_WEEK)) {
                    1 -> getString(R.string.sun)
                    2 -> getString(R.string.mon)
                    3 -> getString(R.string.tue)
                    4 -> getString(R.string.wed)
                    5 -> getString(R.string.thu)
                    6 -> getString(R.string.fri)
                    else -> getString(R.string.sat)
                }

                val hour = getTodayNow().get(Calendar.HOUR_OF_DAY)
                val minute = getTodayNow().get(Calendar.MINUTE)
                val lockToday = getString(R.string.date_info, month.toString(), date.toString(), day)
                binding.lockToday.text = lockToday

                if (hour >= 12) {
                    val lockTime = getString(R.string.time_info, "오후", (hour - 12).toString(), minute.toString())
                    binding.lockTime.text = lockTime
                } else {
                    val lockTime = getString(R.string.time_info, "오전", (hour).toString(), minute.toString())
                    binding.lockTime.text = lockTime
                }

                //각 잠금 타입에 맞게 텍스트 출력
                binding.lockTypeComment.text = when (lockType) {
                    WAIT -> getString(R.string.wait)
                    EXCEED -> getString(R.string.exceed)
                    LOCK_DURATION -> getString(R.string.lock_duration)
                    DIRECT_LOCK -> getString(R.string.direct_lock)
                    else -> ""
                }
            }

            val mUsageStatsManager =
                getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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
                    val topPackageName = checkNotNull(mySortedMap[mySortedMap.lastKey()]?.packageName)
                    if (callPackageName != topPackageName &&
                        topPackageName != messagePackageName && !exceptionAppList.contains(
                            topPackageName
                        )
                    ) { // 예외 리스트 앱에 포함되어 있지 않은 앱이 현재 죄상위(오버레이 제외) 레이아웃에서 실행되고 있다면
                        if (!flag) { // 오버레이가 작동하고 있지 않다면
                            handler.post { // Ui 작업 관련이니까 handler 실행
                                flag = true
                                windowManager.addView(view, params) // 오버레이 실행
                                // 타이머가 기존 작업이 끝났는지 신경쓰지 않고 태스크 실행을 반복해서 너무 짧은 간격으로 설정하다 보니까 겹쳐서 충돌났던 것. 시간을 넉넉하게 줌
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

        Log.d("before Coroutine", "enter")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                loadIconJob.await()
                executor.scheduleWithFixedDelay(
                    runnable, 0, 300, TimeUnit.MILLISECONDS
                ) // 300밀리초마다 스케쥴 돌아가도록 함
            }
        } catch (e: Exception) {
            Log.e("flow Exception", e.toString())
        }
        return START_STICKY
    }

    private fun returnCallPackageName(): String? {
        val callIntent = Intent(Intent.ACTION_DIAL)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                callIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )?.activityInfo?.packageName
        } else {
            packageManager.resolveActivity(
                callIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )?.activityInfo?.packageName
        }
    }

    private fun returnMessagePackageName(): String? {
        return Telephony.Sms.getDefaultSmsPackage(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown() // 타이머 종료
        if (flag) { // 만약 잠금 시간 다 끝나서 서비스 종료하려고 할 때 오버레이가 작동중이었다면 해제하고 종료
            windowManager.removeView(view)
        }
        unregisterReceiver(receiver)
    }

    companion object {
        const val POSSIBLE = 0 //현재 사용 가능 상태
        const val EXCEED = 1    //총 사용 시간을 초과한 상태
        const val LOCK_DURATION = 2 //잠금 시간이 적용된 상태
        const val WAIT = 3  //최소 사용 시간 간격으로 사용할 수 없는 상태
        const val DIRECT_LOCK = 4 //바로 잠금
    }
}
