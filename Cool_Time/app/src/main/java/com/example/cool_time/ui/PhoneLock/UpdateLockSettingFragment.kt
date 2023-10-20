package com.example.cool_time.ui.PhoneLock

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cool_time.ui.CustomCalendarPickerDialog
import com.example.cool_time.ui.CustomTimePickerDialog
import com.example.cool_time.data.LockRepository
import com.example.cool_time.data.UserDatabase
import com.example.cool_time.databinding.FragmentUpdateLockSettingBinding
import com.example.cool_time.model.PhoneLock
import com.example.cool_time.viewmodel.LockViewModel
import com.example.cool_time.viewmodel.LockViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date

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

    private lateinit var time_dialog : CustomTimePickerDialog
    private lateinit var day_dialog : CustomCalendarPickerDialog

    private var db : UserDatabase? = null
    private var repository : LockRepository?= null

    private var lockViewModel : LockViewModel? = null


    private var total_time : Long = 0
    private var min_time : Long = 0
    private var lock_on : Int = 0
    private var lock_off : Int = 0
    private var start_date : Long = 0
    private var end_date : Long = 0

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
    ): View? {
        _binding = FragmentUpdateLockSettingBinding.inflate(inflater, container, false)


        db= UserDatabase.getInstance(activity!!.applicationContext)
        repository = LockRepository(db!!.phoneLockDao())
        lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(repository!!)).get(LockViewModel::class.java)

        // PhoneLock 객체를 이전 프래그먼트에서 받아오는 작업
        val lock = requireArguments().getSerializable("key") as PhoneLock

        receiveLockData(lock)


        binding!!.tvTodayTotalTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "TotalDialog")
        }
        binding!!.tvIntervalTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "IntervalDialog")
        }
        binding!!.tvStartTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "StartTimeDialog")
        }
        binding!!.tvEndTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "EndTimeDialog")
        }
        binding!!.tvStartDay.setOnClickListener{
            day_dialog = CustomCalendarPickerDialog(this)
            day_dialog.show(activity!!.supportFragmentManager, "StartDayDialog")
        }
        binding!!.tvEndDay.setOnClickListener{
            day_dialog = CustomCalendarPickerDialog(this)
            day_dialog.show(activity!!.supportFragmentManager, "EndDayDialog")
        }

        binding.cbNotIntervalSetting.setOnClickListener { //최소 시간 간격 설정 체크박스에 대한 리스너
            if (binding.cbNotIntervalSetting.isChecked) {   //체크되지 않았을 경우
                binding.tvStartTime.isEnabled = false   //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 비활성화
                binding.tvEndTime.isEnabled = false

                //유효하지 않은 값 == -1로 처리
                lock_on = -1
                lock_off = -1

            } else {    //체크한 경우
                binding.tvStartTime.isEnabled = true    //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 활성화
                binding.tvEndTime.isEnabled = true
            }
        }
        binding.cbNotDaySetting.setOnClickListener{//날짜 설정 체크박스에 대한 리스너
            if(binding.cbNotDaySetting.isChecked){  //체크되지 않았을 경우
                binding.tvStartDay.isEnabled = false    //시작 날짜와 종료 날짜 텍스트 뷰의 리스너를 비활성화
                binding.tvEndDay.isEnabled = false

                //유효하지 않은 값 == -1로 처리
                start_date = -1
                end_date = -1
            }
            else{
                binding.tvStartDay.isEnabled = true //시작 날짜와 종료 날짜 텍스트 뷰의 텍스트를 활성화
                binding.tvEndDay.isEnabled = true


            }
        }


        //수정 버튼을 눌렀을 때
        binding.btnUpdateSetting.setOnClickListener {
            //TODO: 업데이트 로직

            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity)
                .setTitle("수정")
                .setMessage("수정하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    //TODO: null check 함수 만들어서 값들이 채워져 있는지를 확인한 후 업데이트 작업할 수 있도록
                    if(contentCheck()){
                        //update 작업, 다시 돌아가기
                        lockViewModel!!.updateLock(
                            PhoneLock(
                                id = lock.id,
                                app_list = emptyList(),
                                total_time = total_time,
                                min_time = min_time,
                                lock_on = lock_on,
                                lock_off = lock_off,
                                lock_day = dayToBit(),
                                start_date = start_date,
                                end_date = end_date
                            )
                        )
                        findNavController().popBackStack()
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
                    lockViewModel!!.deleteLock(lock)
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

        binding.tvTodayTotalTime.text = "${lock.total_time / 60}시간 ${lock.total_time % 60}분"
        binding.tvIntervalTime.text =  "${lock.min_time / 60}시간 ${lock.min_time % 60}분"

        var temp = lock.lock_day

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
        temp -= if(lock.lock_day >= 1) 1 else 0

        if(lock.lock_on == -1 && lock.lock_off == -1){  //특정 시간 잠금 설정하지 않았을 경우
            binding.cbNotIntervalSetting.isChecked = true
            binding.tvStartTime.isEnabled = false
            binding.tvEndTime.isEnabled = false

            binding.tvStartTime.text = "0시간 0분"
            binding.tvEndTime.text = "0시간 0분"
        }
        else {  //특정 시간 잠금을 설정했을 경우
            binding.tvStartTime.text = "${lock.lock_on / 60}시간 ${lock.lock_on % 60}분"
            binding.tvEndTime.text = "${lock.lock_off / 60}시간 ${lock.lock_off % 60}분"
        }

        if(lock.start_date == -1L && lock.end_date == -1L){ //특정 날짜 잠금 설정하지 않은 경우
            binding.cbNotDaySetting.isChecked = true
            binding.tvStartDay.isEnabled = false
            binding.tvEndDay.isEnabled = false

            binding.tvStartDay.text = "시작 날짜"
            binding.tvEndDay.text = "종료 날짜"
        }
        else {  //특정 날짜 잠금 설정한 경우
            binding.tvStartDay.text = SimpleDateFormat("yyyy.MM.dd").format(Date(lock.start_date))
            binding.tvEndDay.text = SimpleDateFormat("yyyy.MM.dd").format(Date(lock.end_date))
        }
        total_time = lock.total_time
        min_time = lock.min_time
        lock_on = lock.lock_on
        lock_off = lock.lock_off
        start_date = lock.start_date
        end_date = lock.end_date

    }

    override fun onYesButtonClick(hour:Int, min:Int){
        when(time_dialog.tag!!){
            "TotalDialog" -> {
                total_time = hour * 60L + min
                binding.tvTodayTotalTime.text = "${hour}시간 ${min}분"
            }
            "IntervalDialog" -> {
                min_time = hour* 60L + min
                binding.tvIntervalTime.text = "${hour}시간 ${min}분"
            }
            "StartTimeDialog" -> {
                lock_on = hour * 60 + min
                binding.tvStartTime.text = String.format("%02d", hour) + " : " + String.format("%02d", min)
            }
            "EndTimeDialog" -> {
                lock_off = hour * 60 + min
                binding.tvEndTime.text = String.format("%02d", hour) + " : " + String.format("%02d", min)
            }
        }
    }
    override fun onYesButtonClick(value:String){
        when(day_dialog.tag!!){
            "StartDayDialog" -> {
                binding.tvStartDay.text = value
                start_date = SimpleDateFormat("yyyy.MM.dd").parse(value).time
            }
            "EndDayDialog" -> {
                binding.tvEndDay.text = value
                end_date = SimpleDateFormat("yyyy.MM.dd").parse(value).time
            }
        }
    }


    private fun dayToBit() : Int{
        var result = 0
        val list = Array<Boolean>(7){false}

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
        when{
            dayToBit() == 0 -> return false //요일 정보를 선택하지 않았을 때
            total_time < min_time -> return false   // 최소 사용 간격 시간이 총 사용량 시간을 초과할 때
            total_time == 0L -> return false // 총 사용 시간이 0시간 0분인 경우
            !binding.cbNotDaySetting.isChecked && //설정 안함을 체크하지 않았는데 시작 날짜나 종료 날짜를 선택하지 않았을 때
                    (binding.tvStartDay.text == "시작 날짜" || binding.tvEndDay.text == "종료 날짜") -> {
            }
            start_date != -1L && end_date != -1L &&     //시작 날짜가 종료 날짜보다 늦을 때
                    SimpleDateFormat("yyyy.MM.dd").parse(binding.tvStartDay.text.toString())!!.time >
                    SimpleDateFormat("yyyy.MM.dd").parse(binding.tvEndDay.text.toString())!!.time
            -> return false

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