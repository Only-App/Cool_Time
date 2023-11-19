package com.onlyapp.cooltime.view.ui.phonelock

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.view.ui.dialog.CustomTimePickerDialog
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentUpdateLockSettingBinding
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.ui.dialog.CustomCalendarPickerDialog
import com.onlyapp.cooltime.view.viewmodel.LockViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateLockSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateLockSettingFragment : Fragment(), CustomTimePickerDialog.ConfirmDialogInterface, CustomCalendarPickerDialog.OnDateChangeListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentUpdateLockSettingBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var timeDialog : CustomTimePickerDialog
    private lateinit var dayDialog : CustomCalendarPickerDialog
    private var db : UserDatabase? = null
    private var repository : LockRepository?= null
    private var lockViewModel : LockViewModel? = null
    private var totalTime : Long = 0
    private var minTime : Long = 0
    private var lockOn : Int = 0
    private var lockOff : Int = 0
    private var startDate : Long = 0
    private var endDate : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateLockSettingBinding.inflate(inflater, container, false)
        activity?.let {activity ->
            db= UserDatabase.getInstance(activity.applicationContext)
            db?.let {db ->
                repository = LockRepository(db.phoneLockDao())
                repository?.let {repository ->
                    lockViewModel = ViewModelProvider(activity, LockViewModelFactory(repository))[LockViewModel::class.java]
                }
            }
        }

        // PhoneLock 객체를 이전 프래그먼트에서 받아오는 작업
        val lock = requireArguments().getSerializable("key") as PhoneLock
        receiveLockData(lock)

        binding.tvTodayTotalTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "TotalDialog")
            }
        }
        binding.tvIntervalTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "IntervalDialog")
            }
        }
        binding.tvStartTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "StartTimeDialog")
            }
        }
        binding.tvEndTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "EndTimeDialog")
            }

        }
        binding.tvStartDay.setOnClickListener{
            dayDialog = CustomCalendarPickerDialog(this)
            activity?.let{
                dayDialog.show(it.supportFragmentManager, "StartDayDialog")
            }
        }
        binding.tvEndDay.setOnClickListener{
            dayDialog = CustomCalendarPickerDialog(this)
            activity?.let{
                dayDialog.show(it.supportFragmentManager, "EndDayDialog")
            }
        }
        binding.cbNotIntervalSetting.setOnClickListener { //최소 시간 간격 설정 체크박스에 대한 리스너
            if (binding.cbNotIntervalSetting.isChecked) {   //체크한 경우
                binding.tvStartTime.isEnabled = false   //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 비활성화
                binding.tvEndTime.isEnabled = false
                //유효하지 않은 값 == -1로 처리
                lockOn = -1
                lockOff = -1
            } else {    //체크하지 않은 경우
                binding.tvStartTime.isEnabled = true    //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 활성화
                binding.tvEndTime.isEnabled = true
            }
        }
        binding.cbNotDaySetting.setOnClickListener{//날짜 설정 체크박스에 대한 리스너
            if(binding.cbNotDaySetting.isChecked){  //체크한 경우
                binding.tvStartDay.isEnabled = false    //시작 날짜와 종료 날짜 텍스트 뷰의 리스너를 비활성화
                binding.tvEndDay.isEnabled = false

                //유효하지 않은 값 == -1로 처리
                startDate = -1
                endDate = -1
            }
            else{   //체크하지 않은 경우
                binding.tvStartDay.isEnabled = true //시작 날짜와 종료 날짜 텍스트 뷰의 텍스트를 활성화
                binding.tvEndDay.isEnabled = true
            }
        }

        //수정 버튼을 눌렀을 때
        binding.btnUpdateSetting.setOnClickListener {
            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity)
                .setTitle("수정")
                .setMessage("수정하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    if(contentCheck()){
                        //update 작업, 다시 돌아가기
                        var duplicateCheck = false
                        lockViewModel?.let {
                            it.lockList.observe(this){ phoneLocks ->
                                phoneLocks.forEach{phoneLock->
                                    if(lock.id != phoneLock.id) {
                                        if (startDate != -1L && endDate != -1L) {  //현재 추가하려는 잠금 정보가 잠금 기간을 설정한 경우
                                            if ((phoneLock.startDate == -1L && phoneLock.endDate == -1L)    //탐색한 잠금 정보가 잠금 기간을 설정하지 않았거나
                                                || (startDate <= phoneLock.startDate && endDate <= phoneLock.endDate)
                                            ) { //탐색한 잠금 정보의 잠금 기간이 현재 추가하려는 잠금 정보의 잠금 기간을 포함할 때
                                                if (dayToBit() and phoneLock.lockDay != 0) {    //겹치는 요일이 존재할 때
                                                    Toast.makeText(
                                                        activity,
                                                        "현재 겹치는 잠금 정보가 존재합니다.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    duplicateCheck = true
                                                }
                                            }
                                        } else {   //현재 추가하려는 잠금 정보가 잠금 기간을 설정하지 않은 경우
                                            if (dayToBit() and phoneLock.lockDay != 0) {
                                                Toast.makeText(
                                                    activity,
                                                    "현재 겹치는 잠금 정보가 존재합니다.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                duplicateCheck = true
                                            }
                                        }
                                    }
                                }
                            }
                            if(!duplicateCheck) {
                                lockViewModel?.updateLock(
                                    PhoneLock(
                                        id = lock.id,
                                        totalTime = totalTime,
                                        minTime = minTime,
                                        lockOn = lockOn,
                                        lockOff = lockOff,
                                        lockDay = dayToBit(),
                                        startDate = startDate,
                                        endDate = endDate
                                    )
                                )

                                findNavController().popBackStack()
                            }
                        }
                    }
                    else Toast.makeText(activity, "잘못된 형식의 입력입니다", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("아니요") {
                    //아니요를 눌렀을 때는 아무 작업도 하지 않도록 함
                        _, _ ->
                }
                .create()
            dialog.show()

        }

        //삭제 버튼을 눌렀을 때
        binding.btnDeleteSetting.setOnClickListener {

            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity)
                .setTitle("삭제")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예"){
                        _,_ ->
                    //delete 작업, 다시 돌아가기
                    lockViewModel?.deleteLock(lock)
                    findNavController().popBackStack()
                }
                .setNegativeButton("아니요"){
                    //아니요를 눌렀을 때 아무 작업도 하지 않도록 함
                        _,_ ->
                }
                .create()

            dialog.show()
        }
        return binding.root
    }

    private fun receiveLockData(lock: PhoneLock){
        val totalTime = "${lock.totalTime / 60}시간 ${lock.totalTime % 60}분"
        val intervalTime = "${lock.minTime / 60}시간 ${lock.minTime % 60}분"
        binding.tvTodayTotalTime.text = totalTime
        binding.tvIntervalTime.text =  intervalTime
        var temp = lock.lockDay

        binding.updateLockCheckMon.isChecked = temp >= 64
        temp -= if(temp >= 64) 64 else 0

        binding.updateLockCheckTues.isChecked = temp >= 32
        temp -= if(temp >= 32) 32 else 0

        binding.updateLockCheckWeds.isChecked = temp >= 16
        temp -= if(temp >= 16) 16 else 0

        binding.updateLockCheckThurs.isChecked = temp >= 8
        temp -= if(temp >= 8) 8 else 0

        binding.updateLockCheckFri.isChecked = temp >= 4
        temp -= if(temp >= 4) 4 else 0

        binding.updateLockCheckSat.isChecked = temp >= 2
        temp -= if(temp >= 2) 2 else 0

        binding.updateLockCheckSun.isChecked = temp >= 1
        temp -= if(lock.lockDay >= 1) 1 else 0

        if(lock.lockOn == -1 && lock.lockOff == -1){  //특정 시간 잠금 설정하지 않았을 경우
            binding.cbNotIntervalSetting.isChecked = true
            binding.tvStartTime.isEnabled = false
            binding.tvEndTime.isEnabled = false
            binding.tvStartTime.text = getString(R.string.time_init2)
            binding.tvEndTime.text = getString(R.string.time_init2)
        }
        else {  //특정 시간 잠금을 설정했을 경우
            val startTime = getString(R.string.time_expression1, String.format("%02d",lock.lockOn / 60), String.format("%02d",lockOn % 60))
            val endTime = getString(R.string.time_expression1, String.format("%02d",lock.lockOff / 60), String.format("%02d",lockOff % 60))
            binding.tvStartTime.text = startTime
            binding.tvEndTime.text = endTime
        }

        if(lock.startDate == -1L && lock.endDate == -1L){ //특정 날짜 잠금 설정하지 않은 경우
            binding.cbNotDaySetting.isChecked = true
            binding.tvStartDay.isEnabled = false
            binding.tvEndDay.isEnabled = false

            binding.tvStartDay.text = "시작 날짜"
            binding.tvEndDay.text = "종료 날짜"
        }
        else {  //특정 날짜 잠금 설정한 경우
            binding.tvStartDay.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(lock.startDate))
            binding.tvEndDay.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(lock.endDate))
        }
        this.totalTime = lock.totalTime
        minTime = lock.minTime
        lockOn = lock.lockOn
        lockOff = lock.lockOff
        startDate = lock.startDate
        endDate = lock.endDate

    }

    override fun onYesButtonClick(hour:Int, min:Int){
        timeDialog.tag?.let {
            when(it){
                "TotalDialog" -> {
                    totalTime = hour * 60L + min
                    val totalTime = "${hour}시간 ${min}분"
                    binding.tvTodayTotalTime.text = totalTime
                }
                "IntervalDialog" -> {
                    minTime = hour* 60L + min
                    val intervalTime = "${hour}시간 ${min}분"
                    binding.tvIntervalTime.text = intervalTime
                }
                "StartTimeDialog" -> {
                    lockOn = hour * 60 + min
                    binding.tvStartTime.text = getString(R.string.time_expression1, String.format("%02d", hour), String.format("%02d", min))
                }
                "EndTimeDialog" -> {
                    lockOff = hour * 60 + min
                    binding.tvEndTime.text = getString(R.string.time_expression1, String.format("%02d", hour), String.format("%02d", min))
                }
            }
        }
    }
    override fun onYesButtonClick(date:String){
        dayDialog.tag?.let {
            when(it){
                "StartDayDialog" -> {
                    binding.tvStartDay.text = date
                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(date)?.let {date->
                        startDate = date.time
                    }
                }
                "EndDayDialog" -> {
                    binding.tvEndDay.text = date
                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(date)?.let {date->
                        endDate = date.time
                    }
                }
                else -> {}
            }
        }

    }

    private fun dayToBit() : Int{
        var result = 0
        val list = Array(7){false}

        list[0] = binding.updateLockCheckMon.isChecked
        list[1] = binding.updateLockCheckTues.isChecked
        list[2] = binding.updateLockCheckWeds.isChecked
        list[3] = binding.updateLockCheckThurs.isChecked
        list[4]  =binding.updateLockCheckFri.isChecked
        list[5] = binding.updateLockCheckSat.isChecked
        list[6] = binding.updateLockCheckSun.isChecked

        //비트 마스킹 작업
        for(check in list){
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }
    //TODO : 내용을 다 입력했는지
    private fun contentCheck() : Boolean{
        if(SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(binding.tvStartDay.text.toString()) != null &&
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(binding.tvEndDay.text.toString()) != null) {
            when {
                dayToBit() == 0 -> return false //요일 정보를 선택하지 않았을 때
                totalTime < minTime -> return false   // 최소 사용 간격 시간이 총 사용량 시간을 초과할 때
                totalTime == 0L -> return false // 총 사용 시간이 0시간 0분인 경우
                !binding.cbNotDaySetting.isChecked && //설정 안함을 체크하지 않았는데 시작 날짜나 종료 날짜를 선택하지 않았을 때
                        (binding.tvStartDay.text == "시작 날짜" || binding.tvEndDay.text == "종료 날짜") -> return false

                startDate != -1L && endDate != -1L &&     //시작 날짜가 종료 날짜보다 늦을 때
                        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(binding.tvStartDay.text.toString())!!.time >
                        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(binding.tvEndDay.text.toString())!!.time
                -> return false

            }
        }
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdateLockSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateLockSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}