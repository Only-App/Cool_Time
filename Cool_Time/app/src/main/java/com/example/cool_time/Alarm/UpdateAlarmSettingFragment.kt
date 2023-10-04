package com.example.cool_time.Alarm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cool_time.AlarmRepository
import com.example.cool_time.UserDatabase
import com.example.cool_time.databinding.FragmentUpdateAlarmSettingBinding
import com.example.cool_time.model.Alarm
import com.example.cool_time.viewmodel.AlarmViewModel
import com.example.cool_time.viewmodel.AlarmViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateAlarmSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateAlarmSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var _binding : FragmentUpdateAlarmSettingBinding? = null
    val binding get() = _binding!!

    private var db : UserDatabase? = null
    private var repository : AlarmRepository?= null
    private var alarmViewModel : AlarmViewModel? = null

    private lateinit var hourPick : NumberPicker // 시간 입력하는 Numberpicker 관리하는 변수
    private lateinit var minPick : NumberPicker // 분 입력하는 Numberpicker 관리하는 변수

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateAlarmSettingBinding.inflate(inflater, container, false)
        hourPick = binding.alaUpdatePicker.hourPicker // _binding이 init 되고 난 후에 값 지정해야 함!
        minPick = binding.alaUpdatePicker.minPicker
        timeInit()
        // 이전 프래그먼트로부터 Alarm 객체를 받아옴
        val alarm  =requireArguments().getSerializable("key") as Alarm
        receiveAlarmData(alarm) //받아온 객체의 값을 다시 뷰 컴포넌트에 보이게 할 수 있도록 함

        db= UserDatabase.getInstance(activity!!.applicationContext)
        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(activity!!, AlarmViewModelFactory(repository!!)).get(AlarmViewModel::class.java)

        binding.btnDeleteSetting.setOnClickListener{
            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity!!)
                .setTitle("삭제")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예"){
                    _, _ ->
                        //삭제 후 이전 화면으로
                        alarmViewModel!!.deleteAlarm(alarm)
                        findNavController().popBackStack()
                }
                .setNegativeButton("아니요"){  //아니요를 눌렀을 때 아무 작업도 하지 않도록
                    _, _ ->
                }
                .create()
            dialog.show()
        }

        binding.btnUpdateSetting.setOnClickListener{
            val etAlarmDescription :String= binding.etAlarmDescription.text.toString()    //알람 내용

            val hour : Int = hourPick.value  //시간
            val minutes : Int= minPick.value   //분

            val total_time = hour * 60 + minutes
            val day_result = dayToBit()

            //입력한 정보를 바탕으로 다시 entity 생성
            val entity=  Alarm(id = alarm.id, name = etAlarmDescription, time = total_time, day = day_result)

            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity!!)

                .setTitle("수정")
                .setMessage("수정하시겠습니까?")
                .setPositiveButton("예"){
                        _, _ ->
                    if(contentCheck()) {    //정보가 모두 입력되었다면
                        //업데이트 후 이전 화면으로
                        alarmViewModel!!.updateAlarm(entity)
                        findNavController().popBackStack()
                    }
                    //아니라면 다시 입력해달라는 메시지 출력
                    else Toast.makeText(activity,"정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("아니요"){  //아니요를 눌렀을 때 아무 작업도 하지 않도록
                        _, _ ->
                }
                .create()
            dialog.show()
        }
        return binding.root
    }

    private fun timeInit(){ // Time Picker 위한 초기 설정
        hourPick.wrapSelectorWheel = false; // 숫자 값을 키보드로 입력하는 것을 막음
        hourPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        minPick.wrapSelectorWheel = false;
        minPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        hourPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        hourPick.maxValue = 23

        minPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        minPick.maxValue = 59
    }

    @SuppressLint("NewApi")     //가져온 Alarm 객체를 바탕으로 layout View에 값을 WRITE
    fun receiveAlarmData(alarm : Alarm){
        binding.etAlarmDescription.setText(alarm.name)
        hourPick.value = alarm.time / 60
        minPick.value = alarm.time % 60

        binding.checkMon.isChecked = alarm.day >= 64
        alarm.day -= if(alarm.day >= 64) 64 else 0

        binding.checkTues.isChecked = alarm.day >= 32
        alarm.day -= if(alarm.day >= 32) 32 else 0

        binding.checkWeds.isChecked = alarm.day >= 16
        alarm.day -= if(alarm.day >= 16) 16 else 0

        binding.checkThurs.isChecked = alarm.day >= 8
        alarm.day -= if(alarm.day >= 8) 8 else 0

        binding.checkFri.isChecked = alarm.day >= 4
        alarm.day -= if(alarm.day >= 4) 4 else 0

        binding.checkSat.isChecked = alarm.day >= 2
        alarm.day -= if(alarm.day >= 64) 2 else 0

        binding.checkSun.isChecked = alarm.day >= 1
        alarm.day -= if(alarm.day >= 1) 1 else 0
    }

    private fun dayToBit() : Int{
        var result = 0
        val list = Array<Boolean>(7){false}

        list[0] = binding.checkMon.isChecked
        list[1] = binding.checkTues.isChecked
        list[2] = binding.checkWeds.isChecked
        list[3] = binding.checkThurs.isChecked
        list[4]  =binding.checkFri.isChecked
        list[5] = binding.checkSat.isChecked
        list[6] = binding.checkSun.isChecked

        //비트 마스킹 작업
        for(check in list){
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }

    private fun contentCheck() : Boolean{
        return !binding.etAlarmDescription.text.isNullOrBlank()
                && dayToBit() != 0
    }
}