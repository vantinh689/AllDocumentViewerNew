package com.reader.office.mychanges.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.reader.office.R
import com.reader.office.databinding.DialogFileSave2Binding
import com.reader.office.mychanges.utils.*
import java.io.File

class FileSaveDialog2(
    context: Context,
    filePath: String,
    defaultFileName: String? = "",
    type: String = "",
    onResult: ((String) -> Unit)? = null
) : Dialog(context, R.style.PauseDialog) {
    var binding: DialogFileSave2Binding

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val params: ViewGroup.LayoutParams = window!!.attributes
        window!!.attributes = params as WindowManager.LayoutParams
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding =
            DialogFileSave2Binding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        binding.fileNameEditText.apply {
            setText(defaultFileName?.changeExtension(""))
            isFocusable = true
            requestFocus()
            maxEms = 10
            filters = arrayOf(InputFilter.LengthFilter(30))
        }
        binding.fileNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(keyword: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if (binding.fileNameEditText.text.toString().startsWith(" "))
                    binding.fileNameEditText.setText("")
            }
        })

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.processBtn.setOnClickListener {
            if (binding.fileNameEditText.text.toString().isNotEmpty()) {
                val newName = if (type == RENAME) {
                    binding.fileNameEditText.setText(binding.fileNameEditText.text.toString().changeExtension(defaultFileName?.getFileNameExtension()?:""))
                    if (!binding.fileNameEditText.text.toString().isValidFileName())
                        binding.fileNameEditText.text.toString() + defaultFileName?.getFileNameExtension()
                    else {
                        binding.fileNameEditText.text.toString()
                    }
                } else {
                    if (!binding.fileNameEditText.text.toString().contains(PDF))
                        binding.fileNameEditText.text.toString() + PDF
                    else
                        binding.fileNameEditText.text.toString()
                }

                val file = File(filePath, newName)
                if (!file.exists()) {
                    val index: Int = filePath.lastIndexOf("/")
                    val dirPath = filePath.substring(0, index + 1)
                    val dir = File(dirPath)
                    Log.d("dir", "dir$dir")
                    val newFile = File(dir, newName)
                    Log.d("newFileDir", "dir${newFile.absolutePath}")
                    if (!newFile.exists()) {
                        dismiss()
                        onResult?.invoke(newFile.name)
                    } else
                        it.snack("file name already exist")
                } else {
                    it.snack("file name already exist")
                }
            } else
                it.snack("Enter name")
        }
        setContentView(binding.root)
    }
}