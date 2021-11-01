package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.RelativeLayout
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ProgressDialogBinding

class ProgressDialog(context: Context) : Dialog(context) {

    lateinit var binding: ProgressDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProgressDialogBinding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        initDialog()
    }

    private fun initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.progress_dialog)
        window!!.decorView.setBackgroundColor(0)
        window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        setCancelable(false)
//        binding.tvMessage.text = context.resources.getString(R.string.wait)
    }

    fun showWithMessage(message: String) {
        show()
//        binding.tvMessage.text = message
    }

    override fun dismiss() {
        if (isShowing)
            super.dismiss()
    }
}