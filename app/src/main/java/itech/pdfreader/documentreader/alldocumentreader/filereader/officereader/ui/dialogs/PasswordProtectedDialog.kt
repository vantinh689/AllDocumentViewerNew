package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogPasswordProtectedBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.showToast
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.snack

class PasswordProtectedDialog(
    title: String? = null,
    context: Context,
    onCancel: (() -> Unit)? = null,
    onResult: ((String) -> Unit)? = null

) : Dialog(context, R.style.PauseDialog) {
    var binding: DialogPasswordProtectedBinding
    private var mPassword: String? = null
    private var isBackPress = true

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val params: ViewGroup.LayoutParams = window!!.attributes
        window!!.attributes = params as WindowManager.LayoutParams
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding =
            DialogPasswordProtectedBinding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        if (title != null)
            binding.title.text = title
        binding.passwordEditText.apply {
            isFocusable = true
            requestFocus()
            maxEms = 8
            filters = arrayOf(InputFilter.LengthFilter(8))
        }
//        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.passwordEditText.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun afterTextChanged(input: Editable?) {
//                if (input?.isEmpty() == true) {
//                    context.showToast(context.getString(R.string.snackbar_password_cannot_be_blank))
//                } else
//
//            }
//
//        })

        binding.cancelBtn.setOnClickListener {
            isBackPress = false
            dismiss()
            onCancel?.invoke()
        }
        binding.processBtn.setOnClickListener {
            if (binding.passwordEditText.text.toString().isNotEmpty()) {
                mPassword = binding.passwordEditText.text.toString()
                onResult?.invoke(mPassword ?: "")
                isBackPress = false
                dismiss()
            } else
                it.snack("Enter password")
        }
        setOnDismissListener {
            if (isBackPress)
                onCancel?.invoke()
        }
        setContentView(binding.root)
    }
}