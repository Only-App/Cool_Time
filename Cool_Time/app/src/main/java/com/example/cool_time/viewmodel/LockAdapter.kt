package com.example.cool_time.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cool_time.databinding.LockSettingItemBinding
import com.example.cool_time.model.PhoneLock
import java.text.SimpleDateFormat
import java.util.Date

class LockAdapter(private val list : List<PhoneLock>, private var mListener : OnLockItemOnClickListener?) :
    RecyclerView.Adapter<LockAdapter.LockViewHolder> (){
        companion object{
            val day_list  = arrayOf("월","화", "수", "목", "금", "토", "일")
            val num_list = arrayOf(64, 32, 16, 8 ,4, 2, 1)


            //선택한 요일 정보(Int type)를 바탕으로 요일을 String으로 변환하도록 함
            fun getDayStr(data : Int) : String{
                if(data == 127){    //일주일 모두 체크한 경우라면
                    return "매일"
                }

                var day_str : String= ""

                var temp = data
                for(i in 0..6){
                    if(temp >= num_list[i]){
                        if(day_str.isNotEmpty())
                            day_str+= ", "
                        day_str+= "${day_list[i]}"
                        temp -= num_list[i]
                    }
                }

                return day_str
            }
        }
        class LockViewHolder(val binding : LockSettingItemBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(lock : PhoneLock){ //일단 그냥 test용 Binding

                binding.tvLockTime.text =
                    if(lock.total_time < 60L)
                            "${lock.total_time % 60}분"
                    else if(lock.total_time % 60 == 0L)
                            "${lock.total_time / 60}시간"
                    else
                        "${lock.total_time / 60}시간 ${lock.total_time % 60}분"


                binding.tvDuration.text =
                    if(lock.start_date == -1L && lock.end_date == -1L)
                        "날짜 설정하지 않음"
                    else SimpleDateFormat("yyyy.MM.dd").format(Date(lock.start_date)) +
                    " ~ " + SimpleDateFormat("yyyy.MM.dd").format(Date(lock.end_date))

                binding.tvDay.text = getDayStr(lock.lock_day)

                binding.tvReuseLock.text = "${lock.min_time}분 내로 재사용 시도시 잠금"
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
            mListener?.onItemClick(list[position], position)
        })
    }
    override fun getItemCount(): Int {
        return list.size
    }

}

interface OnLockItemOnClickListener{
    fun onItemClick(lock : PhoneLock, position : Int)
}

