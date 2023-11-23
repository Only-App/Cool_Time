package com.onlyapp.cooltime.view.ui.phonelock


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.common.showShortToast
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.LockRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentLockSettingBinding
import com.onlyapp.cooltime.model.PhoneLockModel
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.ui.dialog.CustomCalendarPickerDialog
import com.onlyapp.cooltime.view.ui.dialog.CustomTimePickerDialog
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddLockSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddLockSettingFragment : Fragment(), CustomTimePickerDialog.ConfirmDialogInterface, CustomCalendarPickerDialog.OnDateChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentLockSettingBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var timeDialog: CustomTimePickerDialog
    private lateinit var dayDialog: CustomCalendarPickerDialog
    private var db: UserDatabase? = null
    private var repository: LockRepository? = null
    private var lockViewModel: LockViewModel? = null
    private var totalTime: Long = 0
    private var minTime: Long = 0
    private var lockOn: Int = 0
    private var lockOff: Int = 0
    private var startDate: Long = 0
    private var endDate: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentLockSettingBinding.inflate(inflater, container, false)
        activity?.let { activity ->
            db = UserDatabase.getInstance(activity.applicationContext)
            db?.let { db ->
                repository = LockRepositoryImpl(db.phoneLockDao())
                repository?.let { repository ->
                    lockViewModel = ViewModelProvider(
                        activity, LockViewModelFactory(repository)
                    )[LockViewModel::class.java]
                }
            }

        }
        binding.tvStartDay.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(
            getTodayNow().time
        )
        binding.tvEndDay.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(
            getTodayNow().time
        )

        binding.btnAddSetting.setOnClickListener {
            if (contentCheck()) {
                var duplicateCheck = false
                lockViewModel?.let { lockViewModel ->
                    lifecycleScope.launch {
                        lockViewModel.lockModelList.observe(this@AddLockSettingFragment) {
                            it.forEach { phoneLockModel ->
                                if (startDate != -1L && endDate != -1L) {  //현재 추가하려는 잠금 정보가 잠금 기간을 설정한 경우
                                    if ((phoneLockModel.startDate == -1L && phoneLockModel.endDate == -1L)    //탐색한 잠금 정보가 잠금 기간을 설정하지 않았거나
                                        || (startDate <= phoneLockModel.startDate && endDate <= phoneLockModel.endDate)
                                    ) { //탐색한 잠금 정보의 잠금 기간이 현재 추가하려는 잠금 정보의 잠금 기간을 포함할 때
                                        if (dayToBit() and phoneLockModel.lockDay != 0) {    //겹치는 요일이 존재할 때
                                            activity.showShortToast("현재 겹치는 잠금 정보가 존재합니다.")
                                            duplicateCheck = true
                                        }
                                    }
                                } else {   //현재 추가하려는 잠금 정보가 잠금 기간을 설정하지 않은 경우
                                    if (dayToBit() and phoneLockModel.lockDay != 0) {
                                        activity.showShortToast("현재 겹치는 잠금 정보가 존재합니다.")
                                        duplicateCheck = true
                                    }
                                }
                            }
                        }

                        if (!duplicateCheck) {
                            lockViewModel.insertLock(
                                PhoneLockModel(
                                    totalTime = totalTime, minTime = minTime, lockOn = lockOn, lockOff = lockOff, lockDay = dayToBit(), startDate = startDate, endDate = endDate
                                )
                            )

                        }
                        activity.showShortToast("잠금이 설정되었습니다!")
                        findNavController().popBackStack()
                    }
                }
            } else {
                activity.showShortToast("잘못된 형식의 입력입니다")
            }
        }
        binding.tvTodayTotalTime.setOnClickListener {
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "TotalDialog")
            }

        }
        binding.tvIntervalTime.setOnClickListener {
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "IntervalDialog")
            }
        }
        binding.tvStartTime.setOnClickListener {
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "StartTimeDialog")
            }
        }
        binding.tvEndTime.setOnClickListener {
            timeDialog = CustomTimePickerDialog(this)
            activity?.let {
                timeDialog.show(it.supportFragmentManager, "EndTimeDialog")
            }
        }
        binding.tvStartDay.setOnClickListener {
            dayDialog = CustomCalendarPickerDialog(this)
            activity?.let {
                dayDialog.show(it.supportFragmentManager, "StartDayDialog")
            }
        }
        binding.tvEndDay.setOnClickListener {
            dayDialog = CustomCalendarPickerDialog(this)
            activity?.let {
                dayDialog.show(it.supportFragmentManager, "EndDayDialog")
            }
        }

        binding.cbNotIntervalSetting.setOnClickListener { //최소 시간 간격 설정 체크박스에 대한 리스너
            if (binding.cbNotIntervalSetting.isChecked) {   //체크한 경우
                binding.tvStartTime.isEnabled = false   //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 비활성화
                binding.tvEndTime.isEnabled = false

                //유효하지 않은 값 == -1로 처리
                lockOn = -1
                lockOff = -1

            } else {    //체크하지 않은 경우
                binding.tvStartTime.isEnabled = true    //잠금 시작 시간과 잠금 종료 시간 텍스트 뷰의 리스너를 활성화
                binding.tvEndTime.isEnabled = true
            }
        }
        binding.cbNotDaySetting.setOnClickListener {//날짜 설정 체크박스에 대한 리스너
            if (binding.cbNotDaySetting.isChecked) {  //체크한 경우
                binding.tvStartDay.isEnabled = false    //시작 날짜와 종료 날짜 텍스트 뷰의 리스너를 비활성화
                binding.tvEndDay.isEnabled = false
                //유효하지 않은 값 == -1로 처리
                startDate = -1L
                endDate = -1L
            } else {
                binding.tvStartDay.isEnabled = true //시작 날짜와 종료 날짜 텍스트 뷰의 텍스트를 활성화
                binding.tvEndDay.isEnabled = true
            }
        }
        return binding.root
    }

    override fun onYesButtonClick(hour: Int, min: Int) {
        timeDialog.tag?.let { tag ->
            when (tag) {
                "TotalDialog" -> {
                    totalTime = hour * 60L + min
                    binding.tvTodayTotalTime.text = getString(R.string.amount_of_time, hour, min)
                }

                "IntervalDialog" -> {
                    minTime = hour * 60L + min
                    binding.tvIntervalTime.text = getString(R.string.amount_of_time, hour, min)
                }

                "StartTimeDialog" -> {
                    lockOn = hour * 60 + min
                    binding.tvStartTime.text = getString(
                        R.string.time_expression1, String.format("%02d", hour), String.format("%02d", min)
                    )//String.format("%02d", hour) + " : " + String.format("%02d", min)
                    binding.cbNotIntervalSetting.isChecked = false

                }

                "EndTimeDialog" -> {
                    lockOff = hour * 60 + min
                    binding.tvEndTime.text = getString(
                        R.string.time_expression1, String.format("%02d", hour), String.format("%02d", min)
                    )//String.format("%02d", hour) + " : " + String.format("%02d", min)
                    binding.cbNotIntervalSetting.isChecked = false
                }
            }
        }
    }

    override fun onYesButtonClick(date: String) {
        dayDialog.tag?.let { tag ->
            when (tag) {
                "StartDayDialog" -> {
                    binding.tvStartDay.text = date
                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(date)?.let {
                        startDate = it.time
                    }
                }

                "EndDayDialog" -> {
                    binding.tvEndDay.text = date
                    SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(date)?.let {
                        endDate = it.time
                    }
                }

                else -> {}
            }
        }

    }


    private fun dayToBit(): Int {
        var result = 0
        val list = Array(7) { false }

        list[0] = binding.lockCheckMon.isChecked
        list[1] = binding.lockCheckTues.isChecked
        list[2] = binding.lockCheckWeds.isChecked
        list[3] = binding.lockCheckThurs.isChecked
        list[4] = binding.lockCheckFri.isChecked
        list[5] = binding.lockCheckSat.isChecked
        list[6] = binding.lockCheckSun.isChecked

        //비트 마스킹 작업
        for (check in list) {
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }

    private fun contentCheck(): Boolean {
        if (SimpleDateFormat(
                "yyyy.MM.dd", Locale.getDefault()
            ).parse(binding.tvStartDay.text.toString()) != null && SimpleDateFormat(
                "yyyy.MM.dd", Locale.getDefault()
            ).parse(binding.tvEndDay.text.toString()) != null
        ) {
            when {
                dayToBit() == 0 -> return false //요일 정보를 선택하지 않았을 때
                totalTime < minTime -> return false   // 최소 사용 간격 시간이 총 사용량 시간을 초과할 때
                totalTime == 0L -> return false // 총 사용 시간이 0시간 0분인 경우
                !binding.cbNotDaySetting.isChecked && //설정 안함을 체크하지 않았는데 시작 날짜나 종료 날짜를 선택하지 않았을 때
                        (binding.tvStartDay.text == "시작 날짜" || binding.tvEndDay.text == "종료 날짜") -> return false

                startDate != -1L && endDate != -1L &&     //시작 날짜가 종료 날짜보다 늦을 때
                        SimpleDateFormat(
                            "yyyy.MM.dd", Locale.getDefault()
                        ).parse(binding.tvStartDay.text.toString())!!.time > SimpleDateFormat(
                    "yyyy.MM.dd", Locale.getDefault()
                ).parse(binding.tvEndDay.text.toString())!!.time -> return false
            }
        }


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
        fun newInstance(param1: String, param2: String) = AddLockSettingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}