package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.LockSettingItemBinding
import com.onlyapp.cooltime.data.entity.PhoneLock
import java.text.SimpleDateFormat
import java.util.Date

class LockAdapter(private val list : List<PhoneLock>, private var mListener : OnLockItemOnClickListener?) :
    RecyclerView.Adapter<LockAdapter.LockViewHolder> (){
        class LockViewHolder(val binding : LockSettingItemBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(lock : PhoneLock){ //일단 그냥 test용 Binding

                binding.tvLockTime.text =
                    when {
                        lock.totalTime < 60L -> "${lock.totalTime % 60}분"
                        lock.totalTime % 60 == 0L -> "${lock.totalTime / 60}시간"
                        else -> "${lock.totalTime / 60}시간 ${lock.totalTime % 60}분"
                    }


                binding.tvDuration.text =
                    if(lock.startDate == -1L && lock.endDate == -1L)
                        "날짜 설정하지 않음"
                    else SimpleDateFormat("yyyy.MM.dd").format(Date(lock.startDate)) +
                    " ~ " + SimpleDateFormat("yyyy.MM.dd").format(Date(lock.endDate))

                binding.tvDay.text = getDayStr(lock.lockDay)

                binding.tvReuseLock.text = "${lock.minTime}분 내로 재사용 시도시 잠금"
                binding.tvLockRemainTime.text = "0시간 사용시 잠금"     //TODO : 남아있는 시간 계산해서 출력해야 함

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


    companion object{
        private val dayList  = arrayOf("월","화", "수", "목", "금", "토", "일")
        private val numList = arrayOf(64, 32, 16, 8 ,4, 2, 1)


        //선택한 요일 정보(Int type)를 바탕으로 요일을 String으로 변환하도록 함
        fun getDayStr(data : Int) : String{
            if(data == 127){    //일주일 모두 체크한 경우라면
                return "매일"
            }

            var dayStr : String= ""

            var temp = data
            for(i in 0..6){
                if(temp >= numList[i]){
                    if(dayStr.isNotEmpty())
                        dayStr+= ", "
                    dayStr+= "${dayList[i]}"
                    temp -= numList[i]
                }
            }

            return dayStr
        }
    }
}

interface OnLockItemOnClickListener{
    fun onItemClick(lock : PhoneLock, position : Int)
}

