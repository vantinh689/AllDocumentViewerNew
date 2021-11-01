package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.ProgressDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.DocumentUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.DataViewModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels.UtilsViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

@Keep
open class BaseFragment : Fragment() {
    protected val dataViewModel: DataViewModel by activityViewModels()
    protected val utilsViewModel: UtilsViewModel by activityViewModels()
    protected val sharedPref: SharedPref by inject()
    protected val documentUtils:DocumentUtils by inject()
    //protected lateinit var pdfEncryptionUtility : PDFEncryptionUtility
    protected lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        hideSoftKeyboard(this@BaseFragment)
       // pdfEncryptionUtility = PDFEncryptionUtility(requireContext())

    }
    protected fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showToastMessageInCenter(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun showKeyboardOnView(view: View){
        view.post {
            view.requestFocus()
            val imgr = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    open fun hideSoftKeyboard(mFragment: Fragment?) {
        try {
            if (mFragment == null || mFragment.activity == null) {
                return
            }
            val view: View? = mFragment.requireActivity().currentFocus
            if (view != null) {
                val inputManager: InputMethodManager =
                    mFragment.requireActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun navigate(destination: NavDirections, fragmentExtras: FragmentNavigator.Extras? = null) = with(findNavController()) {
            try {
                hideSoftKeyboard(this@BaseFragment)
                currentDestination?.getAction(destination.actionId)
                    ?.let {
                        if (fragmentExtras != null) {
                            navigate(destination, fragmentExtras)
                        } else {
                            navigate(destination)
                        }
                    } ?: kotlin.run {
                    Timber.d("Desired destination not found  \n Current destination : ${this@with.currentDestination?.id} \n Current id : ${this@with.currentDestination?.label}" + "\n Descired Destination ${destination.actionId}")
                    findNavController().navigate(R.id.main_navigation_graph)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun getModuleType() : String {
        return activity?.intent?.getStringExtra(Constants.TOOLS).toString()
    }

//    protected open fun getNavOptions(): NavOptions? {
//        return NavOptions.Builder()
//            .setEnterAnim(R.anim.slide_in_left)
//            .setExitAnim(R.anim.slide_out_right)
//            .setPopEnterAnim(R.anim.slide_in_right)
//            .setPopExitAnim(R.anim.slide_out_left)
//            .build()
//    }

}



