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
import com.example.cool_time.R
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

    private lateinit var timeDialog : CustomTimePickerDialog //시간 선택시 나오게 할 timepicker 다이얼로그
    private lateinit var dayDialog : CustomCalendarPickerDialog // 날짜 선택시 나오게 할 calendar 다이얼로그

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
    ): View {
        // Inflate the layout for this fragment

            _binding = FragmentLockSettingBinding.inflate(inflater, container, false)

            binding.btnAddSetting.setOnClickListener {
                //시작 날짜와 종료 날짜를 Long 타입으로 변환시켜 저장
                val startDay = SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.getDefault()).parse(binding.tvStartDay.text.toString())!!.time
                val endDay = SimpleDateFormat("yyyy년 MM월 dd일", java.util.Locale.getDefault()).parse(binding.tvEndDay.text.toString())!!.time

                // 만약 시작 날짜가 종료 날짜보다 뒤로 설정되어 있다면 다시 설정하도록 안내
                if(startDay > endDay){
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

        //하루 총 사용량, 간격, 시작 시간, 종료시간, 시작 날짜, 종료 날짜마다 구분하기 위해 시간은 TimePicker, 날짜는 Calendar인 별도의 Tag가 등록된 Dialog 생성
        //터치시 dialog 작동
        binding.tvTodayTotalTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            timeDialog.show(activity!!.supportFragmentManager, "TotalDialog")
        }
        binding.tvIntervalTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            timeDialog.show(activity!!.supportFragmentManager, "IntervalDialog")
        }
        binding.tvStartTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            timeDialog.show(activity!!.supportFragmentManager, "StartTimeDialog")
        }
        binding.tvEndTime.setOnClickListener{
            timeDialog = CustomTimePickerDialog(this)
            timeDialog.show(activity!!.supportFragmentManager, "EndTimeDialog")
        }
        binding.tvStartDay.setOnClickListener{
            dayDialog = CustomCalendarPickerDialog(this)
            dayDialog.show(activity!!.supportFragmentManager, "StartDayDialog")
        }
        binding.tvEndDay.setOnClickListener{
            dayDialog = CustomCalendarPickerDialog(this)
            dayDialog.show(activity!!.supportFragmentManager, "EndDayDialog")
        }
        return binding.root
    }
    override fun onYesButtonClick(hour:Int, min:Int){ //CustomTimePickerDialog를 이용한 다이얼로그(시간 선택)에서 등록 버튼을 눌렀을 때 처리할 함수
        Log.d("chk", timeDialog.tag!!)
        when(timeDialog.tag!!){
            //입력했던 값을 올바른 곳에 할당 (시간 설정하고자 눌렀던 곳에 선택한 값 반영)
            "TotalDialog" -> binding.tvTodayTotalTime.text = getString(R.string.amount_of_time, hour, min)
            "IntervalDialog" -> binding.tvIntervalTime.text = getString(R.string.amount_of_time, hour, min)
            "StartTimeDialog" -> binding.tvStartTime.text = getString(R.string.time_expression, hour, min)
            "EndTimeDialog" -> binding.tvEndTime.text = getString(R.string.time_expression, hour, min)
            //코드의 효율성을 위해서인지 text에 "~" 꼴로 바로 대입하지 말고 string.xml에 서식을 입력해서 그걸 이용해 init하라고 warning 뜨는 것 해결
            //"TotalDialog" -> binding.tvTodayTotalTime.text = "${hour}시간 ${min}분"
            //"IntervalDialog" -> binding.tvIntervalTime.text = "${hour}시간 ${min}분"
            //"StartTimeDialog" -> binding.tvStartTime.text = "${hour} : ${min}"
            //"EndTimeDialog" -> binding.tvEndTime.text = "${hour} : ${min}"
        }
    }
    override fun onYesButtonClick(date:String){ //CustomCalendarPickerPickerDialog를 이용한 다이얼로그(날짜 선택)에서 등록 버튼을 눌렀을 때 처리할 함수
        Log.d("chk", date)
        when(dayDialog.tag!!){
            //입력했던 값을 올바른 곳에 할당 (날짜 설정하고자 눌렀던 곳에 선택한 값 반영)
            "StartDayDialog" -> binding.tvStartDay.text = date
            "EndDayDialog" -> binding.tvEndDay.text = date
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
