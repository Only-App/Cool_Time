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
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onlyapp.cooltime.common.showShortToast
import com.onlyapp.cooltime.repository.AlarmRepository
import com.onlyapp.cooltime.repository.AlarmRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentAlarmSettingBinding
import com.onlyapp.cooltime.model.AlarmModel
import com.onlyapp.cooltime.utils.AlarmScheduler
import com.onlyapp.cooltime.view.factory.AlarmViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class AddAlarmSettingFragment : Fragment() {

    private var _binding: FragmentAlarmSettingBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var db: UserDatabase
    private lateinit var repository: AlarmRepository
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var timePicker: TimePicker

    override fun onCreateView( //Fragment가 실행될 때 생명주기에 따라 onCreate 다음으로 자동 실행되는 함수, View 관련 내용을 세팅하기에 적합
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)

        timePicker = binding.alarmTimePicker
        timePicker.setIs24HourView(true)

        timePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS

        val act = checkNotNull(activity) { "Activity is Null" }
        db = checkNotNull(UserDatabase.getInstance(act.applicationContext)) { "Database is Null" }
        repository = AlarmRepositoryImpl(db.alarmDao())
        alarmViewModel = ViewModelProvider(
            act,
            AlarmViewModelFactory(repository!!)
        )[AlarmViewModel::class.java]


        hide() // 다른 곳 누르면 키보드 사라지는 함수
        addAlarmSetting()
        return binding.root
    }

    //키보드 숨기는 함수
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    //키보드 숨기는 함수 Context -> View -> Activity -> Fragment 순으로 hideKeyboard 함수 연속으로 실행되는 구조로 파악
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hide() {
        binding.alaFrame.setOnClickListener {
            hideKeyboard()
        }
        binding.etAlarmDescription.setOnEditorActionListener { _, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE || (event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard() // 엔터나 완료 입력 시 키보드 사라짐
                handled = true
            }
            handled
        }
    }


    private fun addAlarmSetting() {
        //db 객체 가져오기
        //등록 버튼을 누르면 입력한 값들을 바탕으로 Entity객체를 생성하고 Alarm 테이블에 Insert
        binding.btnAddSetting.setOnClickListener {

            val etAlarmDescription: String = binding.etAlarmDescription.text.toString()    //알람 내용

            val hour = timePicker.hour // 시간과 분을 선택된 값으로 대입
            val minutes = timePicker.minute

            //설정한 시간 값(기준값: 분)
            val timeResult = hour * 60 + minutes
            //요일 설정 값(비트 마스크 값)
            val dayResult = dayToBit()

            //Alarm 객체 생성
            val entity =
                AlarmModel(
                    name = etAlarmDescription,
                    day = dayResult,
                    time = timeResult,
                    remainTime = ""
                )

            if (contentCheck()) {
                lifecycleScope.launch {
                    val id = async { alarmViewModel?.insertAlarm(entity) }.await()
                    context?.let {
                        id?.let { id ->
                            AlarmScheduler.registerAlarm(
                                AlarmModel(
                                    id.toInt(),
                                    entity.name,
                                    entity.day,
                                    entity.time,
                                    remainTime = ""
                                ), it
                            )
                        }
                    }

                    findNavController().popBackStack()
                }
            } else {
                activity.showShortToast("정보를 전부 입력해주세요")
            }
        }
    }

    private fun dayToBit(): Int {
        var result = 0
        val list = Array(7) { false }

        list[0] = binding.checkMon.isChecked
        list[1] = binding.checkTues.isChecked
        list[2] = binding.checkWeds.isChecked
        list[3] = binding.checkThurs.isChecked
        list[4] = binding.checkFri.isChecked
        list[5] = binding.checkSat.isChecked
        list[6] = binding.checkSun.isChecked

        //비트 마스킹 작업
        for (check in list) {
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }

    private fun contentCheck(): Boolean {
        return !binding.etAlarmDescription.text.isNullOrBlank()
                && dayToBit() != 0
    }
}