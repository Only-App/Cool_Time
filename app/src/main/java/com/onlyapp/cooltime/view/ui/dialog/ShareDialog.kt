package com.onlyapp.cooltime.view.ui.dialog

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.FragmentShareDialogBinding
import com.onlyapp.cooltime.databinding.ShareTodayInfoBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Scanner

class ShareDialog : DialogFragment() {
    private var _binding : FragmentShareDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShareDialogBinding.inflate(inflater, container, false)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.btnImageShare.setOnClickListener{
            ShareTodayInfoDialog().show(childFragmentManager, null)
            Toast.makeText(activity, "이미지 공유", Toast.LENGTH_SHORT).show()
        }
        binding.btnKakaoShare.setOnClickListener{
            Toast.makeText(activity, "카카오톡 공유", Toast.LENGTH_SHORT).show()
        }
        binding.btnCancel.setOnClickListener{
            dismiss()
        }
    }


}