package com.onlyapp.cooltime.view.ui.phonelock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentPhoneLockBinding
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.view.adapter.LockAdapter
import com.onlyapp.cooltime.view.adapter.OnLockItemOnClickListener
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.launch


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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneLockBinding.inflate(inflater, container, false)
        activity?.let {activity->
            db= UserDatabase.getInstance(activity.applicationContext)
            db?.let {db ->
                repository = LockRepository(db.phoneLockDao())
                repository?.let {repository->
                    lockViewModel = ViewModelProvider(activity, LockViewModelFactory(repository))[LockViewModel::class.java]
                }
            }
        }

        lifecycleScope.launch {    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect { currentUseTime->
                binding.rvLockList.layoutManager = LinearLayoutManager(context)
                lockViewModel?.let {viewModel->
                    viewModel.lockList.observe(viewLifecycleOwner) { list ->
                        binding.rvLockList.adapter = LockAdapter(list, currentUseTime, object : OnLockItemOnClickListener {
                            override fun onItemClick(lock: PhoneLock, position: Int) {
                                val bundle = Bundle()
                                bundle.putSerializable("key", lock)
                                findNavController().navigate(R.id.action_phone_lock_main_to_update_lock_setting, bundle)
                            }
                        })
                    }
                }

            }
        }
        binding.fabAddSetting.setOnClickListener {
            findNavController().navigate(R.id.action_phone_lock_main_to_lock_setting)
        }
        return binding.root
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