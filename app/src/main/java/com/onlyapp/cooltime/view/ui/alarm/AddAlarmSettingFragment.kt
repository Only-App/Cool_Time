package com.onlyapp.cooltime.view.ui.alarm

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentAlarmSettingBinding
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.view.factory.AlarmViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [AddAlarmSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAlarmSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentAlarmSettingBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var hourPick : NumberPicker   // 시간 입력하는 Numberpicker 관리하는 변수, lateinit을 이용해 지금 당장 초기화 하지 않아도 됨, 나중에 binding 후 초기화
    private lateinit var minPick :NumberPicker // 분 입력하는 Numberpicker 관리하는 변수
    private var db : UserDatabase? = null
    private var repository : AlarmRepository?= null


    private var alarmViewModel : AlarmViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView( //Fragment가 실행될 때 생명주기에 따라 onCreate 다음으로 자동 실행되는 함수, View 관련 내용을 세팅하기에 적합
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)
        hourPick = binding.alaTimePicker.hourPicker // _binding이 init 되고 난 후에 값 지정해야 함!
        minPick = binding.alaTimePicker.minPicker   // 아니면 그냥 이 페이지 들어오려고 하면 튕김

        db = UserDatabase.getInstance(activity!!.applicationContext)
        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(activity!!, AlarmViewModelFactory(repository!!)).get(AlarmViewModel::class.java)

        hide() // 다른 곳 누르면 키보드 사라지는 함수
        timeInit() // 시간 설정하는 TimePicker 관련 옵션 설정(숫자 범위 등)
        addAlarmSetting()
        return binding.root
    }

    //키보드 숨기는 함수
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    //키보드 숨기는 함수 Context -> View -> Activity -> Fragment 순으로 hideKeyboard 함수 연속으로 실행되는 구조로 파악
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hide(){
        binding.alaFrame.setOnClickListener{
            hideKeyboard()
        }
        binding.etAlarmDescription.setOnEditorActionListener{ _, actionId, event -> var handled = false
            if(actionId == EditorInfo.IME_ACTION_DONE || (event.action==KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard() // 엔터나 완료 입력 시 키보드 사라짐
                handled = true
            }
            handled
        }
    }

    private fun timeInit(){ // Time Picker 위한 초기 설정
        hourPick.wrapSelectorWheel = false // 숫자 값을 키보드로 입력하는 것을 막음
        hourPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        minPick.wrapSelectorWheel = false
        minPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        hourPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        hourPick.maxValue = 23

        minPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        minPick.maxValue = 59
    }

    private fun addAlarmSetting() {
        //db 객체 가져오기
        db = UserDatabase.getInstance(activity!!.applicationContext)

        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(
            activity!!,
            AlarmViewModelFactory(repository!!)
        ).get(AlarmViewModel::class.java)

        //등록 버튼을 누르면 입력한 값들을 바탕으로 Entity객체를 생성하고 Alarm 테이블에 Insert
        binding.btnAddSetting.setOnClickListener {

            val etAlarmDescription: String = binding.etAlarmDescription.text.toString()    //알람 내용

            val hour = hourPick.value // 시간과 분을 선택된 값으로 대입
            val minutes = minPick.value

            //설정한 시간 값(기준값: 분)
            val time_result = hour * 60 + minutes

            //요일 설정 값(비트 마스크 값)
            val day_result = dayToBit()

            //Alarm 객체 생성
            val entity = Alarm(name = etAlarmDescription, day = day_result, time = time_result)

            if (contentCheck()) {
                alarmViewModel!!.insertAlarm(entity)
                findNavController().popBackStack()
            } else {
                Toast.makeText(activity, "정보를 전부 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
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

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlarmSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddAlarmSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}