package com.example.cool_time.ui.PhoneLock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cool_time.data.LockRepository
import com.example.cool_time.R
import com.example.cool_time.data.UserDatabase
import com.example.cool_time.databinding.FragmentPhoneLockBinding
import com.example.cool_time.model.PhoneLock
import com.example.cool_time.viewmodel.LockAdapter
import com.example.cool_time.viewmodel.LockViewModel
import com.example.cool_time.viewmodel.LockViewModelFactory
import com.example.cool_time.viewmodel.OnLockItemOnClickListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LockOverviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LockOverviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding : FragmentPhoneLockBinding? = null
    val binding get() = _binding!!

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
        // Inflate the layout for this fragment
        _binding = FragmentPhoneLockBinding.inflate(inflater, container, false)

        db= UserDatabase.getInstance(activity!!.applicationContext)
        repository = LockRepository(db!!.phoneLockDao())
        lockViewModel = ViewModelProvider(activity!!, LockViewModelFactory(repository!!)).get(LockViewModel::class.java)

        binding.rvLockList.layoutManager = LinearLayoutManager(this.context)

        lockViewModel!!.lock_list.observe(this, Observer<List<PhoneLock>>{
            list -> binding!!.rvLockList.adapter = LockAdapter(list, object : OnLockItemOnClickListener{
            override fun onItemClick(lock: PhoneLock, position: Int) {
                val bundle  = Bundle()
                bundle.putSerializable("key", lock)
                findNavController().navigate(R.id.action_phone_lock_main_to_update_lock_setting, bundle)
            }
            })
        })
        binding.fabAddSetting.setOnClickListener {
            findNavController().navigate(R.id.action_phone_lock_main_to_lock_setting)
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PhoneLockFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LockOverviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}