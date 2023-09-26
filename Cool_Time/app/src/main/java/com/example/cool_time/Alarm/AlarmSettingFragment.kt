package com.example.cool_time.Alarm

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cool_time.UserDatabase
import com.example.cool_time.databinding.FragmentAlarmSettingBinding
import com.example.cool_time.model.Alarm
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M) //timePicker에서 hour와 minute property 사용이 최소 API를 요구함
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)

        addAlarmSetting()

        return binding.root
    }



    @RequiresApi(Build.VERSION_CODES.M) //timePicker에서 hour와 minute property 사용이 최소 API를 요구함
    private fun addAlarmSetting(){
        //db 객체 가져오기
        val db = UserDatabase.getInstance(activity!!.applicationContext)

        //등록 버튼을 누르면 입력한 값들을 바탕으로 Entity객체를 생성하고 Alarm 테이블에 Insert
        binding!!.btnAddSetting.setOnClickListener {
            Toast.makeText(activity, "REGISTER ALARM SETTING", Toast.LENGTH_SHORT).show()

            val etAlarmDescription :String= binding!!.etAlarmDescription.text.toString()    //알람 내용

            val hour : Int = binding!!.timePicker.hour  //시간
            val minutes : Int= binding!!.timePicker.minute   //분

            //설정한 시간 값(기준값: 분)
            val time_result = hour * 60  + minutes


            //요일 설정 값(비트 마스크 값)
            val day_result = dayToBit()

            //Alarm 객체 생성
            val entity = Alarm(name = etAlarmDescription, day = time_result, time = day_result)


            lifecycleScope.launch(Dispatchers.IO){
                db!!.alarmDao().insertAlarm(entity)
            }

            findNavController().popBackStack()
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