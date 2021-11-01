package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.ProgressDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.DocumentUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.DataViewModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.UtilsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@Keep
open class BaseActivity : AppCompatActivity() {

    val dataViewModel : DataViewModel by viewModel()
    val utilsViewModel : UtilsViewModel by viewModel()
    val documentUtils : DocumentUtils by inject()
    protected val sharedPref: SharedPref by inject()
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = ProgressDialog(this)


    }
    fun showKeyboardOnView(view: View){
        view.post {
            view.requestFocus()
            val imgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}