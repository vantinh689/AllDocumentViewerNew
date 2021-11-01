package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.RatingBar
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.CustomRatebarBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.isRatingDoneFirstTime
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.rateUs
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.sendEmail

class RateDialogNew(var context: Activity) : RatingBar.OnRatingBarChangeListener {
    private var dialog: Dialog? = null
    private var isRateUs = false
    private var isDialogDismiss = false

    lateinit var binding: CustomRatebarBinding

    var isClose = false
    var sharedPref :SharedPref?=null

    fun createRateUsDialog(closeApp: Boolean = false, sharedPref: SharedPref) {
        isClose = closeApp
        this.sharedPref = sharedPref
        dialog = Dialog(context,R.style.PauseDialog)
        dialog?.let { dialog->
            binding = CustomRatebarBinding.inflate(context.layoutInflater)
            dialog.setContentView(binding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            binding.ratingBar.onRatingBarChangeListener = this
            dialog.setCancelable(true)
            if(!closeApp){
                binding.exitBtn.visibility = View.GONE
            }
            binding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            binding.exitBtn.setOnClickListener {
                if (!closeApp) {
                    if (isRateUs) {
                        sharedPref.putBoolean(isRatingDoneFirstTime, true)
                        context.rateUs()
                    } else {
                        context.sendEmail()
                    }
                    dialog.dismiss()
                }
                else {
//                    if (isRateUs) {
//                        context.rateUs()
//                        tinyDB.putBoolean(isRatingDoneFirstTime, true)
//                    } else {
//                        context.sendEmail()
//                    }
                    context.finishAffinity()
                }
            }
            dialog.show()
        }
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        if(rating.toInt() == 5){
            if(!isClose) {
                sharedPref?.putBoolean(isRatingDoneFirstTime, true)
                context.rateUs()
                dialog!!.dismiss()
            }else{
                sharedPref?.putBoolean(isRatingDoneFirstTime, true)
                context.rateUs()
                context.finishAffinity()
            }

        }
        if(rating.toInt() == 4){
            if(!isClose) {
                context.sendEmail()
                dialog!!.dismiss()
            }else{
                context.sendEmail()
                context.finishAffinity()
            }
        }
        isRateUs = rating.toInt() > 3
    }
}