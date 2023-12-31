package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.LockSettingItemBinding
import com.onlyapp.cooltime.model.PhoneLockModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockAdapter(
    private val mListener: ((lock: PhoneLockModel) -> Unit)? = null
) :
    RecyclerView.Adapter<LockAdapter.LockViewHolder>() {
    private val lockDiffer = AsyncListDiffer(this, lockDiffUtil)
    private var currentUseTime: Long = 0

    inner class LockViewHolder(val binding: LockSettingItemBinding) :
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

            binding.tvLockRemainTime.text = remainTime
            binding.llLockItem.setOnClickListener {
                mListener?.invoke(lockModel)
            }
        }
    }
    fun replaceItems(list : List<PhoneLockModel>) {
        lockDiffer.submitList(list)
    }
    fun updateCurrentUseTIme(useTime : Long) {
        currentUseTime = useTime
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = LockSettingItemBinding.inflate(inflater, parent, false)
        return LockViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        holder.bind(lockDiffer.currentList[position])
    }

    override fun getItemCount(): Int {
        return lockDiffer.currentList.size
    }

    companion object {
        private val lockDiffUtil = object : DiffUtil.ItemCallback<PhoneLockModel>(){
            override fun areItemsTheSame(
                oldItem: PhoneLockModel,
                newItem: PhoneLockModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PhoneLockModel,
                newItem: PhoneLockModel
            ): Boolean {
                return oldItem == newItem
            }

        }
        private val dayList = arrayOf("월", "화", "수", "목", "금", "토", "일")
        private val numList = arrayOf(64, 32, 16, 8, 4, 2, 1)

        //선택한 요일 정보(Int type)를 바탕으로 요일을 String으로 변환하도록 함
        fun getDayStr(data: Int): String {
            if (data == 127) return "매일"    //일주일 모두 체크한 경우라면
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
