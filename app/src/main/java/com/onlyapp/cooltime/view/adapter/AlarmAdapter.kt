package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.AlarmItemBinding
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.model.AlarmModel
import com.onlyapp.cooltime.view.adapter.LockAdapter.Companion.getDayStr

class AlarmAdapter(
    private val list: List<AlarmModel>,
    private var mListener:  (alarm : AlarmModel) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: AlarmModel) {    //알람 객체와 Alarm Item Layout을 바인딩

            val dayStr: String = getDayStr(alarm.day)
            val timeStr: String = getTimeStr(alarm.time)

            binding.alarmName.text = alarm.name
            binding.alarmTime.text = timeStr
            binding.alarmDay.text = dayStr
            binding.alarmCompare.text = "몇 분 후 알람이 울립니다"

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
        holder.binding.clAlarmItem.setOnClickListener {
            mListener?.invoke(list[position])
        }
    }

    companion object {
        private val dayList = arrayOf("월", "화", "수", "목", "금", "토", "일")
        private val numList = arrayOf(64, 32, 16, 8, 4, 2, 1)


        //선택한 요일 정보(Int type)를 바탕으로 요일을 String으로 변환하도록 함
        fun getDayStr(data: Int): String {
            if (data == 127) {    //일주일 모두 체크한 경우라면
                return "매일"
            }

            var dayStr = ""

            var temp = data
            for (i in 0..6) {
                if (temp >= numList[i]) {
                    if (dayStr.isNotEmpty())
                        dayStr += ", "
                    dayStr += dayList[i]
                    temp -= numList[i]
                }
            }

            return dayStr
        }

        //Int 타입의 시간 정보를 바탕으로 오전/오후, 시간, 분을 String으로 묶어서 반환하도록 함
        fun getTimeStr(time: Int): String {
            var timeStr = ""

            var hour: Int = time / 60
            val minute: Int = time % 60

            if (hour >= 12) {
                timeStr += "오후 "
                hour -= 12
            } else {
                timeStr += "오전 "
            }

            if (hour < 10) {
                timeStr += "0$hour"
            } else timeStr += hour

            timeStr += " : "

            if (minute < 10) {
                timeStr += "0$minute"
            } else timeStr += minute

            return timeStr
        }

    }


    //TODO 현재 시간 대비 얼마 남았는 지를 계산해서 String Type으로 return할 수 있도록 해야 함
    fun compareTimeStr(): String {
        var result = ""

        return result
    }
}