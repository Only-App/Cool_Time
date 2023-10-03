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
                if(SimpleDateFormat("yyyy년 MM월 dd일").parse(binding.tvStartDay.text.toString())!!.time > SimpleDateFormat("yyyy년 MM월 dd일").parse(binding.tvEndDay.text.toString())!!.time){
                    Toast.makeText(activity, "Please Set Date Correctly", Toast.LENGTH_SHORT).show()
                }
                else {
                    db = UserDatabase.getInstance(activity!!.applicationContext)
                    repository = LockRepository(db!!.phoneLockDao())
                    lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(repository!!)).get(LockViewModel::class.java)

                    //테스트용 insert
                    lockViewModel!!.insertLock(
                        PhoneLock(
                            app_list = emptyList(), total_time = 0, min_time = 0,
                            lock_on = 0, lock_off = 0,
                            start_date = 0, end_date = 0
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
            "TotalDialog" -> binding.tvTodayTotalTime.text = "${hour}시간 ${min}분"
            "IntervalDialog" -> binding.tvIntervalTime.text = "${hour}시간 ${min}분"
            "StartTimeDialog" -> binding.tvStartTime.text = "${hour} : ${min}"
            "EndTimeDialog" -> binding.tvEndTime.text = "${hour} : ${min}"
        }
    }
    override fun onYesButtonClick(value:String){
        Log.d("chk", "${value}")
        when(day_dialog.tag!!){
            "StartDayDialog" -> binding.tvStartDay.text = value
            "EndDayDialog" -> binding.tvEndDay.text = value
        }
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
