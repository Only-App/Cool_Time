package com.example.cool_time.ui.Main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentMainBinding.inflate(inflater, container, false)
/*
        val startday = getTodayStart().timeInMillis
        val endday = getTodayNow().timeInMillis
        val list = load_usage(this.context!!, startday, endday)

        childFragmentManager.beginTransaction().replace(R.id.chart_fragment, ChartAppFragment(list)).commit()

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