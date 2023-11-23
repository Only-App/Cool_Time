package com.onlyapp.cooltime.view.ui.directlock

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.databinding.FragmentDirectLockBinding
import com.onlyapp.cooltime.service.ActiveLockService
import kotlinx.coroutines.launch


class DirectLockFragment : Fragment() {

    private var _binding: FragmentDirectLockBinding? = null
    private val binding
        get() = _binding!!


    private lateinit var timePicker: TimePicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDirectLockBinding.inflate(inflater, container, false)

        binding.fabAddSetting.setOnClickListener {
            lifecycleScope.launch {    //지정한 시간만큼 인내의 시간 증가
                MyApplication.getInstance().getDataStore()
                    .increaseEnduredTime(timePicker.hour * 60 + timePicker.minute)
            }
            val intent = Intent(this.context, ActiveLockService::class.java)
            intent.putExtra(Constants.time, timePicker.hour * 60 * 60 + timePicker.minute * 60)
            activity!!.startService(intent) // 잠금 서비스 실행
        }

        timePicker = binding.directLockTimePicker
        timePicker.setIs24HourView(true)

        timePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS

        return binding.root
    }
}