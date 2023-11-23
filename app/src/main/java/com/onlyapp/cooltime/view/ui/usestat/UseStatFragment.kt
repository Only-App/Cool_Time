package com.onlyapp.cooltime.view.ui.usestat

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.FragmentUseStatBinding
import com.onlyapp.cooltime.view.ui.chart.ChartAppFragment
import com.onlyapp.cooltime.view.ui.chart.ChartHourFragment
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.utils.getTodayStart
import com.onlyapp.cooltime.utils.loadTimeUsage
import com.onlyapp.cooltime.utils.loadUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [UseStatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UseStatFragment : Fragment() {
    private var _binding : FragmentUseStatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUseStatBinding.inflate(layoutInflater, container, false)
        childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment()).commit()
        childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment()).commit()

        lifecycleScope.launch{    //사용 횟수 출력
            MyApplication.getInstance().getDataStore().todayCnt.collect{
                binding.tvStatUseCount.text = it.toString()
            }
        }
        lifecycleScope.launch{
            MyApplication.getInstance().getDataStore().latestUseTime.collect{   //최근 사용 시간 출력
                val sdf= SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.tvStatRecentTime.text = sdf.format(it)
            }
        }

        lifecycleScope.launch{    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect{
                binding.tvUseTime.text =  getString(R.string.time_expression2, String.format("%02d", it / 3600), String.format("%02d", (it % 3600)/60),String.format("%02d", (it % 60)))
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val startDay = getTodayStart().timeInMillis
            val endDay = getTodayNow().timeInMillis
            val todayList =
                withContext(Dispatchers.IO) {
                    this@UseStatFragment.context?.let {
                        loadUsage(it, startDay, endDay)//.await()
                    }
                }
            val hourList =
                withContext(Dispatchers.IO) {
                    this@UseStatFragment.context?.let {
                        loadTimeUsage(it, getTodayStart())
                    }
                }
            todayList?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.app_chart_fragment, ChartAppFragment(it)).commit()
            }
            hourList?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.hour_chart_fragment, ChartHourFragment(it)).commit()
            }
        }
    }

}