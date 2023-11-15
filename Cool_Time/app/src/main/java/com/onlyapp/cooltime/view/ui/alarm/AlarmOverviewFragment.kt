package com.onlyapp.cooltime.view.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.data.AlarmRepository

import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentAlarmBinding
import com.onlyapp.cooltime.model.Alarm
import com.onlyapp.cooltime.view.adapter.AlarmAdapter
import com.onlyapp.cooltime.view.adapter.OnAlarmItemOnClickListener
import com.onlyapp.cooltime.view.factory.AlarmViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [AlarmOverviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlarmOverviewFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentAlarmBinding? = null
    private val binding : FragmentAlarmBinding
        get() = _binding!!


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        //추가 버튼을 누르면 알람 세팅 화면으로 이동
        binding.fabAddAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_alarm_main_to_alarm_setting)
        }


        //TODO: 현재 시간을 바탕으로 몇 시간 몇 분 남았는지를 출력해야 함, 그리고 계속 변할 수 있어야 함
        db = UserDatabase.getInstance(activity!!.applicationContext)
        repository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(activity!!, AlarmViewModelFactory(repository!!)).get(AlarmViewModel::class.java)

        binding.rvAlarmSet.layoutManager = LinearLayoutManager(this.context)

        alarmViewModel!!.alarm_list.observe(this, Observer<List<Alarm>>{
            list -> binding.rvAlarmSet.adapter = AlarmAdapter(list, object : OnAlarmItemOnClickListener {
            override fun onItemClick(alarm: Alarm, pos: Int) {
                val bundle  = Bundle()
                bundle.putSerializable("key", alarm)
                findNavController().navigate(R.id.action_alarm_main_to_update_alarm_setting, bundle)
            }
            })

        })



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
            AlarmOverviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}