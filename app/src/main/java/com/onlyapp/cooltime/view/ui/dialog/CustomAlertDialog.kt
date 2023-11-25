package com.onlyapp.cooltime.view.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.onlyapp.cooltime.databinding.CustomAlertDialogBinding

class CustomAlertDialog(
    private val title: String,
    private val message: String,
    private val positiveButtonListener : () -> Unit,
    ) : DialogFragment() {
    private var _binding: CustomAlertDialogBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CustomAlertDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setMessage(message)
        setPositiveButton(title, positiveButtonListener)
        setNegativeButton("취소")

        val mContext = checkNotNull(context) { return }
        val dialog = checkNotNull(this.dialog) { return }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        super.onViewCreated(view, savedInstanceState)
    }


    private fun setMessage(message: String) {
        binding.alertDialogMessage.text = message
    }

    private fun setPositiveButton(text: String, listener: () -> Unit) {
        binding.alertDialogPositiveButton.text = text
        binding.alertDialogPositiveButton.setOnClickListener{
            listener.invoke()
            dismiss()
        }
    }

    private fun setNegativeButton(text: String) {
        binding.alertDialogNegativeButton.text = text
        binding.alertDialogNegativeButton.setOnClickListener{
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}