package com.onlyapp.cooltime.view.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.AlarmRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentAlarmBinding
import com.onlyapp.cooltime.view.adapter.AlarmAdapter
import com.onlyapp.cooltime.view.factory.AlarmViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch


class AlarmOverviewFragment : Fragment() {
    private var _binding: FragmentAlarmBinding? = null
    private val binding: FragmentAlarmBinding
        get() = _binding!!

    private lateinit var db: UserDatabase
    private lateinit var repository: AlarmRepository
    private lateinit var alarmViewModel: AlarmViewModel

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

        val act = checkNotNull(activity) { "Activity is Null" }
        db = checkNotNull(UserDatabase.getInstance(act.applicationContext)) { "Database is Null" }
        repository = AlarmRepositoryImpl(db.alarmDao())

        alarmViewModel = ViewModelProvider(
            act,
            AlarmViewModelFactory(repository)
        )[AlarmViewModel::class.java]

        val alarmAdapter = AlarmAdapter { alarmModel ->
            val bundle = Bundle()
            bundle.putSerializable("key", alarmModel)
            findNavController().navigate(
                R.id.action_alarm_main_to_update_alarm_setting,
                bundle
            )
        }
        binding.rvAlarmSet.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        lifecycleScope.launch {
            alarmViewModel.alarmModelList.observe(this@AlarmOverviewFragment) { list ->
                alarmAdapter.replaceItems(list)
            }
        }

        return binding.root
    }
}