package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogDeleteFileBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.showToast
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.DataViewModel
import java.io.File


class DeleteFileDialog(activity: Activity, viewPdf: DataModel, dataViewModel: DataViewModel) : Dialog(
    activity
) {

    lateinit var binding: DialogDeleteFileBinding
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val params: ViewGroup.LayoutParams = window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        window!!.attributes = params as WindowManager.LayoutParams
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogDeleteFileBinding.inflate(activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        binding.apply {
            setContentView(root)
            btnYes.setOnClickListener {
                File(viewPdf.path).delete()
                //mainViewModel.deleteFile(viewPdf)
                activity.showToast("File successfully deleted")

                dismiss()
                activity.onBackPressed()
            }
            btnNo.setOnClickListener {
                dismiss()
            }
        }
    }
}