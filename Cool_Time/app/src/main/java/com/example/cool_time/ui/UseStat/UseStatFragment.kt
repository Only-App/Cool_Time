package com.example.cool_time.ui.UseStat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentUseStatBinding
import com.example.cool_time.utils.ChartAppFragment
import com.example.cool_time.utils.ChartHourFragment
import com.example.cool_time.utils.getTodayNow
import com.example.cool_time.utils.getTodayStart
import com.example.cool_time.utils.getTotalTime
import com.example.cool_time.utils.load_time_usage
import com.example.cool_time.utils.load_usage
import com.example.cool_time.utils.totalTimetoText

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
    private lateinit var binding : FragmentUseStatBinding
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
        binding = FragmentUseStatBinding.inflate(inflater, container, false)
        /*
        val startday = getTodayStart().timeInMillis
        val endday = getTodayNow().timeInMillis
        val list = load_usage(this.context!!, startday, endday)

        val hourList = load_time_usage(this.context!!, getTodayStart())


        childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment(hourList)).commit()
        childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment(list)).commit()


         */
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val startday = getTodayStart().timeInMillis
        val endday = getTodayNow().timeInMillis
        val today_list = load_usage(this.context!!, startday, endday)
        val totalTime = getTotalTime(today_list)
        val hourList = load_time_usage(this.context!!, getTodayStart())
        val displayTotalTime = totalTimetoText(totalTime)

        binding.tvUseTime.text = displayTotalTime
        childFragmentManager.beginTransaction().replace(R.id.hour_chart_fragment, ChartHourFragment(hourList)).commit()
        childFragmentManager.beginTransaction().replace(R.id.app_chart_fragment, ChartAppFragment(today_list)).commit()
        val actionbar = (requireActivity() as AppCompatActivity).supportActionBar

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