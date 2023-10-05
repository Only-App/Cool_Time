package com.example.cool_time.PhoneLock

import android.app.AlertDialog
import android.os.Bundle
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
import com.example.cool_time.R
import com.example.cool_time.UserDatabase
import com.example.cool_time.databinding.FragmentUpdateLockSettingBinding
import com.example.cool_time.model.Alarm
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
 * Use the [UpdateLockSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateLockSettingFragment : Fragment(), CustomTimePickerDialog.ConfirmDialogInterface, CustomCalendarPickerDialog.OnDateChangeListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentUpdateLockSettingBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateLockSettingBinding.inflate(inflater, container, false)


        db= UserDatabase.getInstance(activity!!.applicationContext)
        repository = LockRepository(db!!.phoneLockDao())
        lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(repository!!)).get(LockViewModel::class.java)

        // PhoneLock 객체를 이전 프래그먼트에서 받아오는 작업
        val lock = requireArguments().getSerializable("key") as PhoneLock

        receiveLockData(lock)

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

        //수정 버튼을 눌렀을 때
        binding.btnUpdateSetting.setOnClickListener{
            //TODO: 업데이트 로직
            if(SimpleDateFormat("yyyy.MM.dd").parse(binding.tvStartDay.text.toString())!!.time > SimpleDateFormat("yyyy.MM.dd").parse(binding.tvEndDay.text.toString())!!.time){
                Toast.makeText(activity, "Please Set Date Correctly", Toast.LENGTH_SHORT).show()
            }
            else {
                //다이얼로그 출력
                val dialog = AlertDialog.Builder(activity)
                    .setTitle("수정")
                    .setMessage("수정하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        //TODO: null check 함수 만들어서 값들이 채워져 있는지를 확인한 후 업데이트 작업할 수 있도록 함

                        //update 작업, 다시 돌아가기
                        lockViewModel!!.updateLock(lock)
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("아니요") {
                        //아니요를 눌렀을 때는 아무 작업도 하지 않도록 함
                            _, _ ->
                    }
                    .create()

                dialog.show()
            }
        }

        //삭제 버튼을 눌렀을 때
        binding.btnDeleteSetting.setOnClickListener {

            //다이얼로그 출력
            val dialog = AlertDialog.Builder(activity)
                .setTitle("삭제")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예"){
                        _,_ ->
                    //delete 작업, 다시 돌아가기
                    lockViewModel!!.deleteLock(lock)
                    findNavController().popBackStack()
                }
                .setNegativeButton("아니요"){
                    //아니요를 눌렀을 때 아무 작업도 하지 않도록 함
                        _,_ ->
                }
                .create()

            dialog.show()
        }
        return binding.root
    }

    fun receiveLockData(lock: PhoneLock){

        binding.tvTodayTotalTime.text = "${lock.total_time / 60}시간 ${lock.total_time % 60}분"
        binding.tvIntervalTime.text =  "${lock.min_time / 60}시간 ${lock.min_time % 60}분"


        binding.updateLockCheckMon.isChecked = lock.lock_day >= 64
        lock.lock_day -= if(lock.lock_day >= 64) 64 else 0

        binding.updateLockCheckTues.isChecked = lock.lock_day >= 32
        lock.lock_day -= if(lock.lock_day >= 32) 32 else 0

        binding.updateLockCheckWeds.isChecked = lock.lock_day >= 16
        lock.lock_day -= if(lock.lock_day >= 16) 16 else 0

        binding.updateLockCheckThurs.isChecked = lock.lock_day >= 8
        lock.lock_day -= if(lock.lock_day >= 8) 8 else 0

        binding.updateLockCheckFri.isChecked = lock.lock_day >= 4
        lock.lock_day -= if(lock.lock_day >= 4) 4 else 0

        binding.updateLockCheckSat.isChecked = lock.lock_day >= 2
        lock.lock_day -= if(lock.lock_day >= 2) 2 else 0

        binding.updateLockCheckSun.isChecked = lock.lock_day >= 1
        lock.lock_day -= if(lock.lock_day >= 1) 1 else 0

        binding.tvStartTime.text = "${lock.lock_on / 60}시간 ${lock.lock_on % 60}분"
        binding.tvEndTime.text  = "${lock.lock_off / 60}시간 ${lock.lock_off % 60}분"

        binding.tvStartDay.text = SimpleDateFormat("yyyy.MM.dd").format(Date(lock.start_date))
        binding.tvEndDay.text=    SimpleDateFormat("yyyy.MM.dd").format(Date(lock.end_date))

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
         * @return A new instance of fragment UpdateLockSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateLockSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}