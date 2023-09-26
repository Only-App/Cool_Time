package com.example.cool_time.Alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentAlarmBinding
import com.example.cool_time.model.Alarm
import com.example.cool_time.viewmodel.AlarmAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [AlarmFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlarmFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentAlarmBinding? = null
    private val binding : FragmentAlarmBinding
        get() = _binding!!

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
        // Inflate the layout for this fragment
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        //추가 버튼을 누르면 알람 세팅 화면으로 이동
        binding!!.fabAddAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_alarm_main_to_alarm_setting)
        }

        //TODO 테스트용 코드, 다시 지워야 함
        binding!!.btnTest.setOnClickListener {
            findNavController().navigate(R.id.action_alarm_main_to_update_alarm_setting)
        }

        //원래는 getAll함수를 통해서 database(User Database)에 있는 Alarm 정보들을 List로 가져와 어댑터의 파라미터로 전달해야 함
        //현재는 테스트 단계(임의의 샘플 데이터)

        binding.rvAlarmSet.adapter=  AlarmAdapter(
            listOf(Alarm(name = "알람 내용입니다...", day = 15, time = 75), Alarm(name = "아무거나 적습니다", day = 65, time = 800)))
        binding.rvAlarmSet.layoutManager = LinearLayoutManager(this.context)


        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlarmFragment.
         */
        // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlarmFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}