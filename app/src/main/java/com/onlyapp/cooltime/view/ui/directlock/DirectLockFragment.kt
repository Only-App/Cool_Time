package com.onlyapp.cooltime.view.ui.directlock

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.FragmentDirectLockBinding
import com.onlyapp.cooltime.service.ActiveLockService
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DirectLockFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DirectLockFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDirectLockBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var hourPick: NumberPicker   // 시간 입력하는 Numberpicker 관리하는 변수
    private lateinit var minPick: NumberPicker // 분 입력하는 Numberpicker 관리하는 변수

    private lateinit var timePicker : TimePicker
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
        _binding = FragmentDirectLockBinding.inflate(inflater, container, false)

        binding.fabAddSetting.setOnClickListener {
            lifecycleScope.launch {    //지정한 시간만큼 인내의 시간 증가
                MyApplication.getInstance().getDataStore()
                    .increaseEnduredTime(timePicker.hour * 60 + timePicker.minute)
            }
            val intent = Intent(this.context, ActiveLockService::class.java)
            intent.putExtra("time", timePicker.hour * 60 * 60 + timePicker.minute * 60)
            activity!!.startService(intent) // 잠금 서비스 실행
        }

        timePicker = binding.directLockTimePicker
        timePicker.setIs24HourView(true)


        return binding.root
    }

    /*
    private fun timeInit() { // Time Picker 위한 초기 설정
        hourPick.wrapSelectorWheel = false // 숫자 값을 키보드로 입력하는 것을 막음
        hourPick.descendantFocusability =
            NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        minPick.wrapSelectorWheel = false
        minPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        hourPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        hourPick.maxValue = 23

        minPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        minPick.maxValue = 59
    }
     */

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DirectLockFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DirectLockFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}