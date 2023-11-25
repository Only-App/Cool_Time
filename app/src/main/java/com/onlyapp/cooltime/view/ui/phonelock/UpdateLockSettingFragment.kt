package com.onlyapp.cooltime.view.ui.phonelock

import android.app.AlertDialog
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
import com.onlyapp.cooltime.databinding.FragmentUpdateLockSettingBinding
import com.onlyapp.cooltime.model.PhoneLockModel
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.utils.getTodayStart
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.ui.dialog.CustomAlertDialog
import com.onlyapp.cooltime.view.ui.dialog.CustomCalendarPickerDialog
import com.onlyapp.cooltime.view.ui.dialog.CustomTimePickerDialog
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UpdateLockSettingFragment : Fragment(), CustomTimePickerDialog.ConfirmDialogInterface,
    CustomCalendarPickerDialog.OnDateChangeListener {
    private var _binding: FragmentUpdateLockSettingBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateLockSettingBinding.inflate(inflater, container, false)
        activity?.let { activity ->
            db = UserDatabase.getInstance(activity.applicationContext)
            db?.let { db ->
                repository = LockRepositoryImpl(db.phoneLockDao())
                repository?.let { repository ->
                    lockViewModel = ViewModelProvider(
                        activity,
                        LockViewModelFactory(repository)
                    )[LockViewModel::class.java]
                }
            }
        }

        // PhoneLock 객체를 이전 프래그먼트에서 받아오는 작업
        val lock = requireArguments().getSerializable("key") as PhoneLockModel
        receiveLockData(lock)

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
            } else {   //체크하지 않은 경우
                binding.tvStartDay.isEnabled = true //시작 날짜와 종료 날짜 텍스트 뷰의 텍스트를 활성화
                binding.tvEndDay.isEnabled = true

                startDate = getTodayStart().timeInMillis
                endDate = getTodayStart().timeInMillis
            }
        }

        //수정 버튼을 눌렀을 때
        binding.btnUpdateSetting.setOnClickListener {
            //다이얼로그 출력

            val customAlertDialog = CustomAlertDialog("수정", "수정하시겠습니까?") {
                if (contentCheck()) {
                    //update 작업, 다시 돌아가기
                    var duplicateCheck = false
                    lockViewModel?.let {
                        lifecycleScope.launch {
                            it.lockModelList
                                .observe(this@UpdateLockSettingFragment) { lockModelList ->
                                    lockModelList.forEach { lockModel ->
                                        if (lock.id != lockModel.id) {
                                            if (startDate != -1L && endDate != -1L) {  //현재 추가하려는 잠금 정보가 잠금 기간을 설정한 경우
                                                if ((lockModel.startDate == -1L && lockModel.endDate == -1L)    //탐색한 잠금 정보가 잠금 기간을 설정하지 않았거나
                                                    || (startDate <= lockModel.startDate && endDate <= lockModel.endDate)
                                                ) { //탐색한 잠금 정보의 잠금 기간이 현재 추가하려는 잠금 정보의 잠금 기간을 포함할 때
                                                    if (dayToBit() and lockModel.lockDay != 0) {    //겹치는 요일이 존재할 때
                                                        activity.showShortToast("현재 겹치는 잠금 정보가 존재합니다.")
                                                        duplicateCheck = true
                                                    }
                                                }
                                            } else {   //현재 추가하려는 잠금 정보가 잠금 기간을 설정하지 않은 경우
                                                if (dayToBit() and lockModel.lockDay != 0) {
                                                    activity.showShortToast("현재 겹치는 잠금 정보가 존재합니다.")
                                                    duplicateCheck = true
                                                }
                                            }
                                        }
                                    }
                                }
                            if (!duplicateCheck) {
                                lockViewModel?.updateLock(
                                    PhoneLockModel(
                                        id = lock.id,
                                        totalTime = totalTime,
                                        minTime = minTime,
                                        lockOn = lockOn,
                                        lockOff = lockOff,
                                        lockDay = dayToBit(),
                                        startDate = startDate,
                                        endDate = endDate
                                    )
                                )

                                findNavController().popBackStack()
                            }
                        }
                    }
                } else activity.showShortToast("잘못된 형식의 입력입니다.")
            }

            customAlertDialog.show(parentFragmentManager, null)
        }

        //삭제 버튼을 눌렀을 때
        binding.btnDeleteSetting.setOnClickListener {

            val customAlertDialog = CustomAlertDialog("삭제", "삭제하시겠습니까?") {
                lockViewModel?.deleteLock(lock)
                findNavController().popBackStack()
            }

            customAlertDialog.show(parentFragmentManager, null)
            //다이얼로그 출력

        }
        return binding.root
    }

    private fun receiveLockData(lockModel: PhoneLockModel) {
        val totalTime = "${lockModel.totalTime / 60}시간 ${lockModel.totalTime % 60}분"
        val intervalTime = "${lockModel.minTime / 60}시간 ${lockModel.minTime % 60}분"
        binding.tvTodayTotalTime.text = totalTime
        binding.tvIntervalTime.text = intervalTime
        var temp = lockModel.lockDay

        binding.updateLockCheckMon.isChecked = temp >= 64
        temp -= if (temp >= 64) 64 else 0

        binding.updateLockCheckTues.isChecked = temp >= 32
        temp -= if (temp >= 32) 32 else 0

        binding.updateLockCheckWeds.isChecked = temp >= 16
        temp -= if (temp >= 16) 16 else 0

        binding.updateLockCheckThurs.isChecked = temp >= 8
        temp -= if (temp >= 8) 8 else 0

        binding.updateLockCheckFri.isChecked = temp >= 4
        temp -= if (temp >= 4) 4 else 0

        binding.updateLockCheckSat.isChecked = temp >= 2
        temp -= if (temp >= 2) 2 else 0

        binding.updateLockCheckSun.isChecked = temp >= 1
        temp -= if (lockModel.lockDay >= 1) 1 else 0

        if (lockModel.lockOn == -1 && lockModel.lockOff == -1) {  //특정 시간 잠금 설정하지 않았을 경우
            binding.cbNotIntervalSetting.isChecked = true
            binding.tvStartTime.isEnabled = false
            binding.tvEndTime.isEnabled = false
            binding.tvStartTime.text = getString(R.string.time_init2)
            binding.tvEndTime.text = getString(R.string.time_init2)
        } else {  //특정 시간 잠금을 설정했을 경우
            val startTime = getString(
                R.string.time_expression1,
                String.format("%02d", lockModel.lockOn / 60),
                String.format("%02d", lockOn % 60)
            )
            val endTime = getString(
                R.string.time_expression1,
                String.format("%02d", lockModel.lockOff / 60),
                String.format("%02d", lockOff % 60)
            )
            binding.tvStartTime.text = startTime
            binding.tvEndTime.text = endTime
        }

        if (lockModel.startDate == -1L && lockModel.endDate == -1L) { //특정 날짜 잠금 설정하지 않은 경우
            binding.cbNotDaySetting.isChecked = true
            binding.tvStartDay.isEnabled = false
            binding.tvEndDay.isEnabled = false

            binding.tvStartDay.text = SimpleDateFormat(
                getString(R.string.date_pattern),
                Locale.getDefault()
            ).format(
                getTodayNow().time
            )
            binding.tvEndDay.text =
                SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).format(
                    getTodayNow().time
                )

        } else {  //특정 날짜 잠금 설정한 경우
            binding.tvStartDay.text =
                SimpleDateFormat(
                    getString(R.string.date_pattern),
                    Locale.getDefault()
                ).format(Date(lockModel.startDate))
            binding.tvEndDay.text =
                SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).format(
                    Date(
                        lockModel.endDate
                    )
                )
        }

        this.totalTime = lockModel.totalTime
        minTime = lockModel.minTime
        lockOn = lockModel.lockOn
        lockOff = lockModel.lockOff
        startDate = lockModel.startDate
        endDate = lockModel.endDate

    }

    override fun onYesButtonClick(hour: Int, min: Int) {
        timeDialog.tag?.let {
            when (it) {
                "TotalDialog" -> {
                    totalTime = hour * 60L + min
                    val totalTime = "${hour}시간 ${min}분"
                    binding.tvTodayTotalTime.text = totalTime
                }

                "IntervalDialog" -> {
                    minTime = hour * 60L + min
                    val intervalTime = "${hour}시간 ${min}분"
                    binding.tvIntervalTime.text = intervalTime
                }

                "StartTimeDialog" -> {
                    lockOn = hour * 60 + min
                    binding.tvStartTime.text = getString(
                        R.string.time_expression1,
                        String.format("%02d", hour),
                        String.format("%02d", min)
                    )
                }

                "EndTimeDialog" -> {
                    lockOff = hour * 60 + min
                    binding.tvEndTime.text = getString(
                        R.string.time_expression1,
                        String.format("%02d", hour),
                        String.format("%02d", min)
                    )
                }
            }
        }
    }

    override fun onYesButtonClick(date: String) {
        dayDialog.tag?.let {
            when (it) {
                "StartDayDialog" -> {
                    binding.tvStartDay.text = date
                    SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).parse(
                        date
                    )?.let { date ->
                        startDate = date.time
                    }
                }

                "EndDayDialog" -> {
                    binding.tvEndDay.text = date
                    SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).parse(
                        date
                    )?.let { date ->
                        endDate = date.time
                    }
                }

                else -> {}
            }
        }

    }

    private fun dayToBit(): Int {
        var result = 0
        val list = Array(7) { false }

        list[0] = binding.updateLockCheckMon.isChecked
        list[1] = binding.updateLockCheckTues.isChecked
        list[2] = binding.updateLockCheckWeds.isChecked
        list[3] = binding.updateLockCheckThurs.isChecked
        list[4] = binding.updateLockCheckFri.isChecked
        list[5] = binding.updateLockCheckSat.isChecked
        list[6] = binding.updateLockCheckSun.isChecked

        //비트 마스킹 작업
        for (check in list) {
            result *= 2
            result += if (check) 1 else 0
        }
        return result
    }

    private fun contentCheck(): Boolean {

        if (SimpleDateFormat(
                getString(R.string.date_pattern),
                Locale.getDefault()
            ).parse(binding.tvStartDay.text.toString()) != null &&
            SimpleDateFormat(
                getString(R.string.date_pattern),
                Locale.getDefault()
            ).parse(binding.tvEndDay.text.toString()) != null
        ) {
            when {
                dayToBit() == 0 -> return false //요일 정보를 선택하지 않았을 때
                totalTime < minTime -> return false   // 최소 사용 간격 시간이 총 사용량 시간을 초과할 때
                totalTime == 0L -> return false // 총 사용 시간이 0시간 0분인 경우
                /*
                !binding.cbNotDaySetting.isChecked && //설정 안함을 체크하지 않았는데 시작 날짜나 종료 날짜를 선택하지 않았을 때
                        (binding.tvStartDay.text == "시작 날짜" || binding.tvEndDay.text == "종료 날짜") -> return false
                 */
                startDate != -1L && endDate != -1L &&     //시작 날짜가 종료 날짜보다 늦을 때
                        SimpleDateFormat(
                            getString(R.string.date_pattern),
                            Locale.getDefault()
                        ).parse(binding.tvStartDay.text.toString())!!.time >
                        SimpleDateFormat(
                            getString(R.string.date_pattern),
                            Locale.getDefault()
                        ).parse(binding.tvEndDay.text.toString())!!.time
                -> return false

            }
        }
        return true
    }


}