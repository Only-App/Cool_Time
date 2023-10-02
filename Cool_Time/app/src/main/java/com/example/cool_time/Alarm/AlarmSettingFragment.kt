package com.example.cool_time.Alarm

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cool_time.AlarmRepository
import com.example.cool_time.UserDatabase
import com.example.cool_time.databinding.FragmentAlarmSettingBinding
import com.example.cool_time.model.Alarm
import com.example.cool_time.viewmodel.AlarmViewModel
import com.example.cool_time.viewmodel.AlarmViewModelFactory
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [AlarmSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlarmSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentAlarmSettingBinding? = null
    private val binding
        get() = _binding!!

    private var db :UserDatabase? = null
    private var repository : AlarmRepository?= null


    private var alarmViewModel : AlarmViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M) //timePicker에서 hour와 minute property 사용이 최소 API를 요구함
    override fun onCreateView( //Fragment가 실행될 때 생명주기에 따라 onCreate 다음으로 자동 실행되는 함수, View 관련 내용을 세팅하기에 적합
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)

        db = UserDatabase.getInstance(activity!!.applicationContext)
        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(activity!!, AlarmViewModelFactory(repository!!)).get(AlarmViewModel::class.java)

        hide()
        alarmInit()
        addAlarmSetting()
        return binding.root
    }

    //키보드 숨기는 함수
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }
    //키보드 숨기는 함수 Context -> View -> Activity -> Fragment 순으로 hideKeyboard 함수 연속으로 실행되는 구조로 파악
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun hide(){

        binding!!.alaFrame.setOnClickListener{
            hideKeyboard()
        }

        binding!!.etAlarmDescription.setOnEditorActionListener{v, actionId, event -> var handled = false
            if(actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyboard() // 엔터나 완료 입력 시 키보드 사라짐
                handled = true
            }
            handled
        }

    }

    fun alarmInit(){ // Time Picker 위한 초기 설정
        binding!!.alaHourPicker.wrapSelectorWheel = false; // 숫자 값을 키보드로 입력하는 것을 막음
        binding!!.alaHourPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        binding!!.alaMinPicker.wrapSelectorWheel = false;
        binding!!.alaMinPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        binding!!.alaHourPicker.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        binding!!.alaHourPicker.maxValue = 23

        binding!!.alaMinPicker.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        binding!!.alaMinPicker.maxValue = 59
    }

    @RequiresApi(Build.VERSION_CODES.M) //timePicker에서 hour와 minute property 사용이 최소 API를 요구함
    private fun addAlarmSetting() {
        //db 객체 가져오기
        db = UserDatabase.getInstance(activity!!.applicationContext)

        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(
            activity!!,
            AlarmViewModelFactory(repository!!)
        ).get(AlarmViewModel::class.java)


        //등록 버튼을 누르면 입력한 값들을 바탕으로 Entity객체를 생성하고 Alarm 테이블에 Insert
        binding!!.btnAddSetting.setOnClickListener {

            val etAlarmDescription: String = binding!!.etAlarmDescription.text.toString()    //알람 내용

            var hour = binding!!.alaHourPicker.value // 시간과 분을 선택된 값으로 대입
            var minutes = binding!!.alaMinPicker.value

            /*
            기본적으로 addAlarmSetting 함수가 확인 버튼을 눌렀을 때 작동하는 함수이니까
            setOnValueChangedListener로 실시간으로 값을 갱신시킬 필요는 없다고 판단
            그러나 혹시나 나중에 값이 제대로 안들어갈 경우에 위 코드 사용
            정상 작동 확인될 시 위 주석 처리 코드 삭제

            binding!!.alaHourPicker.setOnValueChangedListener{time, before, after ->
                hour = after
            }
            binding!!.alaMinPicker.setOnValueChangedListener{time, before, after ->
                minutes = after
            }
            */

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

        list[0] = binding!!.checkMon.isChecked
        list[1] = binding!!.checkTues.isChecked
        list[2] = binding!!.checkWeds.isChecked
        list[3] = binding!!.checkThurs.isChecked
        list[4]  =binding!!.checkFri.isChecked
        list[5] = binding!!.checkSat.isChecked
        list[6] = binding!!.checkSun.isChecked

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
            AlarmSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}