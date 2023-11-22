package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.LockSettingItemBinding
import com.onlyapp.cooltime.model.PhoneLockModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockAdapter(
    private val list: List<PhoneLockModel>,
    private val currentUseTime: Long,
    private var mListener: (lock : PhoneLockModel) -> Unit
) :
    RecyclerView.Adapter<LockAdapter.LockViewHolder>() {
    class LockViewHolder(val binding: LockSettingItemBinding, private val currentUseTime: Long) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lockModel: PhoneLockModel) {
            binding.tvLockTime.text =
                when {
                    lockModel.totalTime < 60L -> "${lockModel.totalTime % 60}분"
                    lockModel.totalTime % 60 == 0L -> "${lockModel.totalTime / 60}시간"
                    else -> "${lockModel.totalTime / 60}시간 ${lockModel.totalTime % 60}분"
                }

            binding.tvDuration.text =
                if (lockModel.startDate == -1L && lockModel.endDate == -1L)
                    "날짜 설정하지 않음"
                else SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale.getDefault()
                ).format(Date(lockModel.startDate)) +
                        " ~ " + SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale.getDefault()
                ).format(Date(lockModel.endDate))

            binding.tvDay.text = getDayStr(lockModel.lockDay)
            val reuseLock = "${lockModel.minTime}분 내로 재사용 시도시 잠금"

            binding.tvReuseLock.text = reuseLock
            val remain = lockModel.totalTime * 60 - currentUseTime

            val remainTime =
                (remain / 3600).toString() + "시간 " + (remain % 3600 / 60).toString() + "분 " + remain % 60 + "초 더 사용시 잠금"

            binding.tvLockRemainTime.text =
                remainTime // currentUseTime     //TODO : 남아있는 시간 계산해서 출력해야 함

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = LockSettingItemBinding.inflate(inflater, parent, false)
        return LockViewHolder(itemBinding, currentUseTime)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        holder.bind(list[position])
        holder.binding.llLockItem.setOnClickListener {
            mListener?.invoke(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
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
    }
}
