package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogGotoPageBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.showKeyboardOnView
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.snack
import java.io.File

class GotoPageDialog(
    activity: Activity,
    onResult: ((String) -> Unit)? = null
) : Dialog(activity, R.style.PauseDialog) {
    var binding: DialogGotoPageBinding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val params: ViewGroup.LayoutParams = window!!.attributes
        window!!.attributes = params as WindowManager.LayoutParams
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogGotoPageBinding.inflate(activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        binding.pageNoEdt.apply {
            isFocusable = true
            requestFocus()
        }

        activity.showKeyboardOnView(binding.pageNoEdt)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.processBtn.setOnClickListener {
            if (binding.pageNoEdt.text.toString().isNotEmpty()) {
                dismiss()
                onResult?.invoke(binding.pageNoEdt.text.toString())
            } else
                it.snack("Enter Page no")
        }
        setContentView(binding.root)
    }
}