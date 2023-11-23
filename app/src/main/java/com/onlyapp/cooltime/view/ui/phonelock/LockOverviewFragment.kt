package com.onlyapp.cooltime.view.ui.phonelock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.LockRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentPhoneLockBinding
import com.onlyapp.cooltime.view.adapter.LockAdapter
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.launch


class LockOverviewFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentPhoneLockBinding? = null
    val binding get() = _binding!!

    private var db: UserDatabase? = null
    private var repository: LockRepository? = null

    private var lockViewModel: LockViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneLockBinding.inflate(inflater, container, false)
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

        lifecycleScope.launch {    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect { currentUseTime ->
                binding.rvLockList.layoutManager = LinearLayoutManager(context)
                lockViewModel?.let { viewModel ->
                    lifecycleScope.launch {
                        viewModel.lockModelList.observe(viewLifecycleOwner) { list ->
                            binding.rvLockList.adapter = LockAdapter(list, currentUseTime) { lock ->
                                val bundle = Bundle()
                                bundle.putSerializable("key", lock)
                                findNavController().navigate(
                                    R.id.action_phone_lock_main_to_update_lock_setting,
                                    bundle
                                )
                            }
                        }
                    }
                }

            }
        }
        binding.fabAddSetting.setOnClickListener {
            findNavController().navigate(R.id.action_phone_lock_main_to_lock_setting)
        }
        return binding.root
    }

}