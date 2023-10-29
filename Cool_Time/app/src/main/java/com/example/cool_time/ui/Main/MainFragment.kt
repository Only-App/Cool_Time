package com.example.cool_time.ui.Main

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.cool_time.MyApplication
import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentMainBinding
import com.example.cool_time.utils.ChartAppFragment
import com.example.cool_time.utils.getDiff
import com.example.cool_time.utils.getTodayNow
import com.example.cool_time.utils.getTodayStart
import com.example.cool_time.utils.getTotalTime
import com.example.cool_time.utils.getYesterdayEnd
import com.example.cool_time.utils.getYesterdayStart
import com.example.cool_time.utils.load_usage
import com.example.cool_time.utils.totalTimetoText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentMainBinding? = null
    val binding get() = _binding!!
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
        _binding= FragmentMainBinding.inflate(inflater, container, false)
        childFragmentManager.beginTransaction().replace(R.id.chart_fragment, ChartAppFragment()).commit()

        CoroutineScope(Dispatchers.Main).launch{//사용 횟수 출력
            MyApplication.getInstance().getDataStore().todayCnt.collect{
                val yesterdayCnt =MyApplication.getInstance().getDataStore().yesterdayCnt.first()
                binding.tvUseCount.text = it.toString()
                binding.tvCompareUseCnt.text=
                    if(it < yesterdayCnt){
                        "어제보다 ${yesterdayCnt - it}회 덜 사용"
                    }
                    else{
                        "어제보다 ${it - yesterdayCnt}회 더 사용"
                    }
            }
        }
        CoroutineScope(Dispatchers.Main).launch{
            MyApplication.getInstance().getDataStore().latestUseTime.collect {   //최근 사용 시간 출력
                val sdf = SimpleDateFormat("HH:mm")
                binding.tvRecentTime.text = sdf.format(it)

            }
        }

        CoroutineScope(Dispatchers.Main).launch{    //인내의 시간 출력
            MyApplication.getInstance().getDataStore().enduredTime.collect{
                val yesterdayEnduredTime = MyApplication.getInstance().getDataStore().yesterdayEnduredTime.first()
                binding.tvEnduredTime.text = "%02d:%02d".format(it / 60, it % 60)

                binding.tvCompareEndure.text =
                    if(it < yesterdayEnduredTime){
                        "어제보다 %02d시간 %02d분 덜 잠금".format((yesterdayEnduredTime -it) / 60, (yesterdayEnduredTime - it) % 60)
                    }
                    else "어제보다 %02d시간 %02d분 더 잠금".format((it - yesterdayEnduredTime) / 60, (it - yesterdayEnduredTime) % 60)

            }
        }
        /*
        CoroutineScope(Dispatchers.Main).launch{    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect{
                val yesterdayUseTime = MyApplication.getInstance().getDataStore().yesterdayUseTime.first()
                var diff = yesterdayUseTime - it
                if(diff < 0) diff = -diff
                binding.tvUseTime.text =  "%02d : %02d : %02d".format(it / 3600, (it % 3600) /  60, it % 60)
                binding.tvCompareUseTime.text =
                    if(it < yesterdayUseTime){
                        "어제보다 ${diff / 3600}시간 ${diff % 3600 / 60}분 ${diff % 60}초 덜 사용"
                    }
                    else "어제보다 ${diff / 3600}시간 ${diff % 3600 / 60}분 ${diff % 60}초 더 사용"
            }
        }
         */
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val startday = getTodayStart().timeInMillis
        val endday = getTodayNow().timeInMillis
        val today_list = load_usage(this.context!!, startday, endday)
        val totalTime = getTotalTime(today_list)

        val displayTotalTime = totalTimetoText(totalTime)
        binding.tvUseTime.text = displayTotalTime

        val startyesterday = getYesterdayStart().timeInMillis
        val endyesterday = getYesterdayEnd().timeInMillis
        val yesterday_list = load_usage(this.context!!, startyesterday, endyesterday)
        val yesterdayTotalTime = getTotalTime(yesterday_list)

        val displaydiffTime = getDiff(totalTime, yesterdayTotalTime)
        binding.tvCmpUseTime.text = displaydiffTime

        childFragmentManager.beginTransaction().replace(R.id.chart_fragment, ChartAppFragment(today_list)).commit()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}