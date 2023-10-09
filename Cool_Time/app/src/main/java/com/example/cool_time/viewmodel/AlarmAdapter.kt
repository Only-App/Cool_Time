package com.example.cool_time.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cool_time.databinding.AlarmItemBinding
import com.example.cool_time.model.Alarm

class AlarmAdapter(private val list : List<Alarm>, private var mListener : OnAlarmItemOnClickListener?): RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>(){

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

        //Int 타입의 시간 정보를 바탕으로 오전/오후, 시간, 분을 String으로 묶어서 반환하도록 함
        fun getTimeStr(time : Int) : String{
            var time_str = ""

            var hour : Int = time / 60
            var minute : Int = time % 60

            if(hour >= 12){
                time_str += "오후 "
                hour -= 12
            }
            else{
                time_str += "오전 "
            }

            if(hour < 10){
                time_str += "0$hour"
            }
            else time_str += hour

            time_str += " : "

            if(minute < 10){
                time_str += "0$minute"
            }
            else time_str += minute

            return time_str
        }

    }


    //TODO 현재 시간 대비 얼마 남았는 지를 계산해서 String Type으로 return할 수 있도록 해야 함
    fun compareTimeStr() : String{
        var result  = ""

        return result
    }
    class AlarmViewHolder(val binding : AlarmItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(alarm : Alarm){    //알람 객체와 Alarm Item Layout을 바인딩

            var day_str : String=  getDayStr(alarm.day)
            var time_str : String = getTimeStr(alarm.time)


            binding.alarmName.text = alarm.name
            binding.alarmTime.text = time_str
            binding.alarmDay.text = day_str
            binding.alarmCompare.text= "어쩌구 저쩌구"



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = AlarmItemBinding.inflate(inflater, parent, false) //layout inflate 작업
        return AlarmViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(list[position])
        holder.binding.clAlarmItem.setOnClickListener(View.OnClickListener {
            mListener?.onItemClick(list[position], position)
        })
    }
}


interface OnAlarmItemOnClickListener{
    fun onItemClick(alarm : Alarm, pos :Int)
}