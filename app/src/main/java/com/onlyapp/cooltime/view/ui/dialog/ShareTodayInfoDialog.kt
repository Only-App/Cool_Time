package com.onlyapp.cooltime.view.ui.dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentShareTodayInfoDialogBinding
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ShareTodayInfoDialog : DialogFragment() {
    private fun getViewToBitmap(): Bitmap {
        val view = binding.info
        val bitmap = Bitmap.createBitmap(
            view.root.width, view.root.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.root.draw(canvas)
        return bitmap
    }

    private fun saveImage() {
        try {
            context?.let {
                it.externalCacheDir?.let{path -> path.mkdir()}
                val file = File(it.externalCacheDir, "today_info.jpg")

                val bitmap =getViewToBitmap()
                Log.d("cache", it.externalCacheDir.toString())
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                loadImage()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadImage(){
        try {
            context?.let {
                val file = File(it.externalCacheDir, "today_info.jpg")
                shareImage(FileProvider.getUriForFile(it, "image_share", file))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareImage(uri: Uri){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent,"Share Image"))
    }



    private var _binding:FragmentShareTodayInfoDialogBinding?= null
    private val binding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set dialog width
        val width = (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% of screen width
        val height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        dialog?.window?.setLayout(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareTodayInfoDialogBinding.inflate(inflater, container, false)
        binding.cancle.setOnClickListener{
            dismiss()
        }
        binding.check.setOnClickListener {
            saveImage()
        }
        lifecycleScope.launch{//사용 횟수 출력
            MyApplication.getInstance().getDataStore().todayCnt.collect{
                val yesterdayCnt = MyApplication.getInstance().getDataStore().yesterdayCnt.first()
                binding.info.tvUseCount.text = it.toString()
                binding.info.tvCompareUseCnt.text=
                    if(it < yesterdayCnt){
                        "어제보다 ${yesterdayCnt - it}회 덜 사용"
                    }
                    else{
                        "어제보다 ${it - yesterdayCnt}회 더 사용"
                    }
            }
        }

        lifecycleScope.launch{
            MyApplication.getInstance().getDataStore().latestUseTime.collect {   //최근 사용 시간 출력
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.info.tvRecentTime.text = sdf.format(it)

            }
        }

        lifecycleScope.launch{    //인내의 시간 출력
            MyApplication.getInstance().getDataStore().enduredTime.collect{
                val yesterdayEnduredTime = MyApplication.getInstance().getDataStore().yesterdayEnduredTime.first()
                binding.info.tvEnduredTime.text = "%02d:%02d".format(it / 60, it % 60)

                binding.info.tvCompareEndure.text =
                    if(it < yesterdayEnduredTime){
                        "어제보다 %02d시간 %02d분 덜 잠금".format((yesterdayEnduredTime -it) / 60, (yesterdayEnduredTime - it) % 60)
                    }
                    else "어제보다 %02d시간 %02d분 더 잠금".format((it - yesterdayEnduredTime) / 60, (it - yesterdayEnduredTime) % 60)

            }
        }

        lifecycleScope.launch{    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect{
                Log.d("TotalTimeChanged", it.toString())
                val yesterdayUseTime = MyApplication.getInstance().getDataStore().yesterdayUseTime.first()
                var diff = yesterdayUseTime - it
                if(diff < 0) diff = -diff
                binding.info.tvUseTime.text =  "%02d : %02d : %02d".format(it / 3600, (it % 3600) /  60, it % 60)
                binding.info.tvCmpUseTime.text =
                    if(it < yesterdayUseTime){
                        "어제보다 ${diff / 3600}시간 ${diff % 3600 / 60}분 ${diff % 60}초 덜 사용"
                    } else "어제보다 ${diff / 3600}시간 ${diff % 3600 / 60}분 ${diff % 60}초 더 사용"
            }
        }
        context?.let { context ->
            val lockDao = checkNotNull(UserDatabase.getInstance(context)?.phoneLockDao())
            val lockRepository = LockRepository(lockDao)
            val lockViewModel = LockViewModel(lockRepository)
            Log.d("tstst", "여기까지 진입")
            lockViewModel.lockList.observe(this@ShareTodayInfoDialog) { list ->
                var flag = false
                for (lockInfo in list) {
                    val startDate = lockInfo.startDate
                    val endDate = lockInfo.endDate
                    val lockDay = lockInfo.lockDay
                    if ((startDate == -1L && endDate == -1L) || getTodayNow().timeInMillis in startDate..endDate) {
                        val dayOfWeek =
                            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        //7비트를 이용하여 월~일 값 비트마스팅으로 설정 => Calendar.getInstance().get(Calendar.DAY_OF_WEEK)가 일~토 => 1 ~ 7 로 반환 => 월요일은 1000000, 일요일은 0000001 꼴
                        if ((1 shl (6 - (dayOfWeek + 5) % 7)) and lockDay != 0) { //오늘이 설정한 요일에 포함되는지
                            val totalTime = (lockInfo.totalTime / 60).toString() + "시간 " + (lockInfo.totalTime % 60).toString() + "분"
                            binding.info.tvTodayTotalTime.text = totalTime
                            val intervalTime = (lockInfo.minTime / 60).toString() + "시간 " + (lockInfo.minTime % 60).toString() + "분"
                            binding.info.tvIntervalTime.text = intervalTime
                            if (lockInfo.lockOn == -1) {
                                val lockOn = "∞"
                                val lockOff = "∞"
                                binding.info.tvStartTime.text = lockOn
                                binding.info.tvEndTime.text = lockOff
                            } else {
                                val lockOn = (lockInfo.lockOn / 60).toString() + "시간 " + (lockInfo.lockOn % 60).toString() + "분"
                                val lockOff = (lockInfo.lockOff / 60).toString() + "시간 " + (lockInfo.lockOff % 60).toString() + "분"
                                binding.info.tvStartTime.text = lockOn
                                binding.info.tvEndTime.text = lockOff
                            }
                            flag = true
                            break
                        }
                    }
                }
                if (!flag) {
                    binding.info.llTodayLock.visibility = View.GONE
                    binding.info.llTodayLockText.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }
}

