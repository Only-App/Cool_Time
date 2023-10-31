package com.example.cool_time.ui.UseStat

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.cool_time.MyApplication
import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentUseStatBinding
import com.example.cool_time.utils.ChartAppFragment
import com.example.cool_time.utils.ChartHourFragment
import com.example.cool_time.utils.getTodayNow
import com.example.cool_time.utils.getTodayStart
import com.example.cool_time.utils.getTotalTime
import com.example.cool_time.utils.loadTimeUsageAsync
import com.example.cool_time.utils.loadUsageAsync
import com.example.cool_time.utils.load_time_usage
import com.example.cool_time.utils.load_usage
import com.example.cool_time.utils.totalTimetoText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UseStatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UseStatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentUseStatBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        /*
        val startday = getTodayStart().timeInMillis
        val endday = getTodayNow().timeInMillis
        val list = load_usage(this.context!!, startday, endday)

        val hourList = load_time_usage(this.context!!, getTodayStart())


        childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment(hourList)).commit()
        childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment(list)).commit()


         */

        _binding = FragmentUseStatBinding.inflate(layoutInflater, container, false)

        childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment()).commit()
        childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment()).commit()


        CoroutineScope(Dispatchers.Main).launch{    //사용 횟수 출력
            MyApplication.getInstance().getDataStore().todayCnt.collect{
                binding.tvStatUseCount.text = it.toString()
            }
        }
        CoroutineScope(Dispatchers.Main).launch{
            MyApplication.getInstance().getDataStore().latestUseTime.collect{   //최근 사용 시간 출력
                val sdf= SimpleDateFormat("HH:mm")
                binding.tvStatRecentTime.text = sdf.format(it)
            }
        }
/*
        CoroutineScope(Dispatchers.Main).launch{    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect{
                binding.tvUseTime.text =  "%02d : %02d : %02d".format(it / 3600, it % 3600 /  60, it % 60)
            }

        }
 */


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch{
            val startday = getTodayStart().timeInMillis
            val endday = getTodayNow().timeInMillis
            val today_list = loadUsageAsync(this@UseStatFragment.context!!, startday, endday).await()
            val totalTime = getTotalTime(today_list)
            //val hourList = load_time_usage(this.context!!, getTodayStart())
            val hourList = loadTimeUsageAsync(this@UseStatFragment.context!!, getTodayStart()).await()
            val displayTotalTime = totalTimetoText(totalTime)


            binding.tvUseTime.text = displayTotalTime
            childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment(hourList)).commit()
            childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment(today_list)).commit()
            val actionbar = (requireActivity() as AppCompatActivity).supportActionBar
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UseStatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UseStatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}