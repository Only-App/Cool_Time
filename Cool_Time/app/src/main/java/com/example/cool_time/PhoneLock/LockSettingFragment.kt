package com.example.cool_time.PhoneLock

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cool_time.CustomCalendarPickerDialog
import com.example.cool_time.CustomTimePickerDialog
import com.example.cool_time.LockRepository
import com.example.cool_time.UserDatabase
import com.example.cool_time.databinding.FragmentLockSettingBinding
import com.example.cool_time.model.PhoneLock
import com.example.cool_time.viewmodel.LockViewModel
import com.example.cool_time.viewmodel.LockViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LockSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LockSettingFragment : Fragment(), CustomTimePickerDialog.ConfirmDialogInterface, CustomCalendarPickerDialog.OnDateChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentLockSettingBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var time_dialog : CustomTimePickerDialog
    private lateinit var day_dialog : CustomCalendarPickerDialog

    private var db : UserDatabase? = null
    private var repository : LockRepository?= null

    private var lockViewModel : LockViewModel? = null

    private var total_time : Long = 0
    private var min_time : Long = 0
    private var lock_on : Int = 0
    private var lock_off : Int = 0
    private var start_date : Long = 0
    private var end_date : Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

            _binding = FragmentLockSettingBinding.inflate(inflater, container, false)

            binding!!.btnAddSetting.setOnClickListener {
                if(SimpleDateFormat("yyyy.MM.dd").parse(binding.tvStartDay.text.toString())!!.time > SimpleDateFormat("yyyy.MM.dd").parse(binding.tvEndDay.text.toString())!!.time){
                    Toast.makeText(activity, "Please Set Date Correctly", Toast.LENGTH_SHORT).show()
                }
                else {
                    db = UserDatabase.getInstance(activity!!.applicationContext)
                    repository = LockRepository(db!!.phoneLockDao())
                    lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(repository!!)).get(LockViewModel::class.java)

                    //테스트용 insert
                    lockViewModel!!.insertLock(
                        PhoneLock(
                            app_list = emptyList(), total_time = total_time, min_time = min_time,
                            lock_on = lock_on, lock_off = lock_off, lock_day = dayToBit(),
                            start_date = start_date, end_date = end_date
                        )
                    )

                    Toast.makeText(activity, "REGISTER LOCK SETTING", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }

        binding!!.tvTodayTotalTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "TotalDialog")
        }
        binding!!.tvIntervalTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "IntervalDialog")
        }
        binding!!.tvStartTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "StartTimeDialog")
        }
        binding!!.tvEndTime.setOnClickListener{
            time_dialog = CustomTimePickerDialog(this)
            time_dialog.show(activity!!.supportFragmentManager, "EndTimeDialog")
        }
        binding!!.tvStartDay.setOnClickListener{
            day_dialog = CustomCalendarPickerDialog(this)
            day_dialog.show(activity!!.supportFragmentManager, "StartDayDialog")
        }
        binding!!.tvEndDay.setOnClickListener{
            day_dialog = CustomCalendarPickerDialog(this)
            day_dialog.show(activity!!.supportFragmentManager, "EndDayDialog")
        }
        return binding.root
    }
    override fun onYesButtonClick(hour:Int, min:Int){
        Log.d("chk", time_dialog.tag!!)
        when(time_dialog.tag!!){
            "TotalDialog" -> {
                total_time = hour * 60L + min
                binding.tvTodayTotalTime.text = "${hour}시간 ${min}분"
            }
            "IntervalDialog" -> {
                min_time = hour* 60L + min
                binding.tvIntervalTime.text = "${hour}시간 ${min}분"
            }
            "StartTimeDialog" -> {
                lock_on = hour * 60 + min
                binding.tvStartTime.text = String.format("%02d", hour) + " : " + String.format("%02d", min)
            }
            "EndTimeDialog" -> {
                lock_off = hour * 60 + min
                binding.tvEndTime.text = String.format("%02d", hour) + " : " + String.format("%02d", min)
            }
        }
    }
    override fun onYesButtonClick(value:String){
        Log.d("chk", "${value}")
        when(day_dialog.tag!!){
            "StartDayDialog" -> {
                binding.tvStartDay.text = value
                start_date = SimpleDateFormat("yyyy.MM.dd").parse(value).time
            }
            "EndDayDialog" -> {
                binding.tvEndDay.text = value
                end_date = SimpleDateFormat("yyyy.MM.dd").parse(value).time
            }
        }
    }



    private fun dayToBit() : Int{
        var result = 0
        val list = Array<Boolean>(7){false}

        list[0] = binding.lockCheckMon.isChecked
        list[1] = binding.lockCheckTues.isChecked
        list[2] = binding.lockCheckWeds.isChecked
        list[3] = binding.lockCheckThurs.isChecked
        list[4]  =binding.lockCheckFri.isChecked
        list[5] = binding.lockCheckSat.isChecked
        list[6] = binding.lockCheckSun.isChecked

        //비트 마스킹 작업
        for(check in list){
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }

    private fun contentCheck() : Boolean{
       return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LockSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LockSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
