package com.example.cool_time.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cool_time.databinding.LockSettingItemBinding
import com.example.cool_time.model.PhoneLock

class LockAdapter(private val list : List<PhoneLock>, private var mListener : OnLockItemOnClickListener) :
    RecyclerView.Adapter<LockAdapter.LockViewHolder> (){
        class LockViewHolder(val binding : LockSettingItemBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(lock : PhoneLock){ //일단 그냥 test용 Binding
                binding.tvLockTime.text = "6시간"
                binding.tvDuration.text = "23.00.00 ~ 23.00.00"
                binding.tvDay.text = "월, 화, 수"
                binding.tvReuseLock.text = "0분 내로 재사용 시도시 잠금"
                binding.tvLockRemainTime.text = "0시간 사용시 잠금"

            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = LockSettingItemBinding.inflate(inflater, parent, false)
        return LockViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        holder.bind(list[position])
        holder.binding.llLockItem.setOnClickListener(View.OnClickListener {
            mListener.onItemClick(list[position], position)
        })
    }
    override fun getItemCount(): Int {
        return list.size
    }

}

interface OnLockItemOnClickListener{
    fun onItemClick(lock : PhoneLock, position : Int)
}