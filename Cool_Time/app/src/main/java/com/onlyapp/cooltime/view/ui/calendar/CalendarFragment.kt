package com.onlyapp.cooltime.view.ui.calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.AlarmRepository
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentCalendarBinding
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.utils.ChartAppFragment
import com.onlyapp.cooltime.utils.ChartHourFragment
import com.onlyapp.cooltime.utils.getSomedayEnd
import com.onlyapp.cooltime.utils.getSomedayStart
import com.onlyapp.cooltime.utils.loadTimeUsage
import com.onlyapp.cooltime.utils.loadUsage
import com.onlyapp.cooltime.view.adapter.AlarmAdapter
import com.onlyapp.cooltime.view.adapter.LockAdapter
import com.onlyapp.cooltime.view.factory.AlarmViewModelFactory
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.AlarmViewModel
import com.onlyapp.cooltime.view.viewmodel.DateViewModel
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var db : UserDatabase? = null
    private var lockRepository : LockRepository?= null
    private var alarmRepository: AlarmRepository? = null

    private var lockViewModel : LockViewModel? = null
    private var alarmViewModel : AlarmViewModel? = null

    private val dateViewModel : DateViewModel by viewModels()

    private var _binding : FragmentCalendarBinding? = null
    private val binding get() = _binding!!

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
        // Inflate the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)


        binding.llLockAndAlarmSet.visibility = View.VISIBLE
        binding.llChart.visibility = View.GONE

        db= UserDatabase.getInstance(activity!!.applicationContext)
        lockRepository = LockRepository(db!!.phoneLockDao())
        lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(lockRepository!!)).get(LockViewModel::class.java)

        alarmRepository = AlarmRepository(db!!.alarmDao())
        alarmViewModel = ViewModelProvider(activity!!, AlarmViewModelFactory(alarmRepository!!)).get(AlarmViewModel::class.java)


        dateViewModel.date.value = binding.calendarView.date    //dateViewModel date 속성 값 초기화

        binding.calendarView.setOnDateChangeListener {  //캘린더 뷰 날짜 변경 리스너 설정
                calendarView, year, month, day ->

            lifecycleScope.launch {
                val date = Date(binding.calendarView.date)

                val startday = getSomedayStart(year, month, day)
                val endday = getSomedayEnd(year, month, day)

                val appList = withContext(Dispatchers.IO) {
                    loadUsage(
                        this@CalendarFragment.context!!,
                        startday.timeInMillis,
                        endday.timeInMillis
                    )
                }
                val hourList = withContext(Dispatchers.IO) {
                    loadTimeUsage(
                        this@CalendarFragment.context!!,
                        getSomedayStart(
                            startday.get(Calendar.YEAR),
                            startday.get(Calendar.MONTH),
                            startday.get(Calendar.DAY_OF_MONTH)
                        )
                    )
                }
                childFragmentManager.beginTransaction()
                    .replace(binding.hourChartFragment.id, ChartHourFragment(hourList)).commit()
                childFragmentManager.beginTransaction()
                    .replace(binding.appChartFragment.id, ChartAppFragment(appList)).commit()

                dateViewModel.date.value =
                    SimpleDateFormat("yyyy.MM.dd").parse("$year.${month + 1}.$day")!!.time
                //dateViewModel의 date 속성 값 변경
            }
        }

        //RecyclerView 설정 작업
        binding.rvCalendarLockSet.layoutManager = LinearLayoutManager(this.context)
        binding.rvCalendarAlarmSet.layoutManager = LinearLayoutManager(this.context)

        //date 값이 변경되는지 관찰
        dateViewModel.date.observe(this, Observer<Long>{
            date_time ->
            lockViewModel!!.lockList.observe(this, Observer<List<PhoneLock>>{
                    list -> //조건에 맞는 list만 filtering 작업
                val filteredList = list.filter{ elem -> (elem.startDate == -1L && elem.endDate == -1L) ||
                        date_time in elem.startDate..elem.endDate
                }
                if(filteredList.isNullOrEmpty()) {  //조건에 맞는 잠금 정보가 없는 경우
                    binding.tvLock.text = "잠금 정보가 존재하지 않습니다"
                }
                else {  //조건에 맞는 잠금 정보가 존재하는 경우
                    binding.tvLock.text = "잠금"
                }
                binding.rvCalendarLockSet.adapter = LockAdapter(filteredList, null) //filteredList를 가지고 어댑터 연결

            })

            alarmViewModel!!.alarmList.observe(this, Observer<List<Alarm>>{
                    list ->
                val filteredList = list.filter{elem ->
                    var cal = Calendar.getInstance()
                    cal.time = Date(date_time)
                    //비트 연산을 통해서 선택한 날짜의 요일이 알람 정보의 요일 정보에 포함되는지를 판단
                    (1 shl  (6- (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)) and elem.day != 0
                }
                //조건에 맞는 알람 정보가 존재하지 않은 경우
                if(filteredList.isNullOrEmpty()){
                    binding.tvAlarm.text = "알람 정보가 존재하지 않습니다"
                }
                else binding.tvAlarm.text= "알람"
                binding.rvCalendarAlarmSet.adapter = AlarmAdapter(filteredList, null)   //filteredList를 가지고 어댑터 연결
            })

            val date = Date(date_time)



        })





        binding.radioGroup.setOnCheckedChangeListener{
            _, _ ->
            if(binding.btnLockAndAlarm.isChecked) {
                binding.llLockAndAlarmSet.visibility = View.VISIBLE
                binding.llChart.visibility = View.GONE
            }
            else{
                binding.llLockAndAlarmSet.visibility = View.GONE
                binding.llChart.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch{
            val date = Date(binding.calendarView.date)

            val startDay = getSomedayStart(date.year + 1900, date.month, date.date)
            val endDay = getSomedayEnd(date.year + 1900, date.month, date.date)

            val appList = withContext(Dispatchers.IO) {
                loadUsage(
                    this@CalendarFragment.context!!,
                    startDay.timeInMillis,
                    endDay.timeInMillis
                )
            }
            val hourList =
                withContext(Dispatchers.IO) {
                    loadTimeUsage(
                        this@CalendarFragment.context!!,
                        getSomedayStart(
                            startDay.get(Calendar.YEAR),
                            startDay.get(Calendar.MONTH),
                            startDay.get(Calendar.DAY_OF_MONTH)
                        )
                    )
                }
            childFragmentManager.beginTransaction()
                .replace(R.id.hour_chart_fragment, ChartHourFragment(hourList)).commit()
            childFragmentManager.beginTransaction()
                .replace(binding.appChartFragment.id, ChartAppFragment(appList)).commit()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}