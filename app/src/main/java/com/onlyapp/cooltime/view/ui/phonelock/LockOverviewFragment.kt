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
import com.onlyapp.cooltime.repository.LockRepository
import com.onlyapp.cooltime.repository.LockRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentPhoneLockBinding
import com.onlyapp.cooltime.view.adapter.LockAdapter
import com.onlyapp.cooltime.view.factory.LockViewModelFactory
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.launch


class LockOverviewFragment : Fragment() {
    private var _binding: FragmentPhoneLockBinding? = null
    val binding get() = _binding!!

    private lateinit var db: UserDatabase
    private lateinit var repository: LockRepository
    private lateinit var lockViewModel: LockViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneLockBinding.inflate(inflater, container, false)
        val act = checkNotNull(activity) { "Activity is Null" }
        db = checkNotNull(UserDatabase.getInstance(act.applicationContext)) { "Database is Null" }
        repository = LockRepositoryImpl(db.phoneLockDao())
        lockViewModel = ViewModelProvider(
            act,
            LockViewModelFactory(repository)
        )[LockViewModel::class.java]

        val lockAdapter = LockAdapter { lock ->
            val bundle = Bundle()
            bundle.putSerializable("key", lock)
            findNavController().navigate(
                R.id.action_phone_lock_main_to_update_lock_setting,
                bundle
            )
        }

        binding.rvLockList.apply {
            adapter = lockAdapter
            layoutManager = LinearLayoutManager(context)
        }
        lifecycleScope.launch {    //총 사용 시간 출력
            MyApplication.getInstance().getDataStore().todayUseTime.collect { currentUseTime ->
                lockAdapter.updateCurrentUseTIme(currentUseTime)
            }
        }
        lifecycleScope.launch{
            lockViewModel.lockModelList.observe(viewLifecycleOwner) { list ->
                lockAdapter.replaceItems(list)
            }
        }

        binding.fabAddSetting.setOnClickListener {
            findNavController().navigate(R.id.action_phone_lock_main_to_lock_setting)
        }
        return binding.root
    }

}