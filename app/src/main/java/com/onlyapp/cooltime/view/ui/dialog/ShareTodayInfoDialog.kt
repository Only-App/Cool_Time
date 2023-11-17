package com.onlyapp.cooltime.view.ui.dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.onlyapp.cooltime.databinding.FragmentShareTodayInfoDialogBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ShareTodayInfoDialog : DialogFragment() {
    private fun getViewToBitmap(): Bitmap {
        val view = binding.info
        val bitmap = Bitmap.createBitmap(
            view.root.width, view.root.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.root.draw(canvas)
        return bitmap
    }

    private fun saveImage() {
        try {
            context?.let {
                it.externalCacheDir?.let{it.mkdir()}
                val file = File(it.externalCacheDir, "today_info.jpg")

                val bitmap =getViewToBitmap()
                Log.d("cache", it.externalCacheDir.toString())
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                loadImage()
                Log.d("data.toByteArray()", "saved")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadImage(){
        try {
            context?.let {
                val file = File(it.externalCacheDir, "today_info.jpg")
                it.grantUriPermission("com.kakao.talk" , FileProvider.getUriForFile(it, "image_share", file), Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareImage(FileProvider.getUriForFile(it, "image_share", file))

                Log.d("FileInputStream", "")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareImage(uri: Uri){
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent,"Share Image"))
    }



    private var _binding:FragmentShareTodayInfoDialogBinding?= null
    private val binding
        get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //set dialog width
        val width = (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% of screen width
        val height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        dialog?.window?.setLayout(width, height)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareTodayInfoDialogBinding.inflate(inflater, container, false)
        binding.cancle.setOnClickListener{
            dismiss()
        }
        binding.check.setOnClickListener {
            saveImage()
        }

        return binding.root
    }
}

