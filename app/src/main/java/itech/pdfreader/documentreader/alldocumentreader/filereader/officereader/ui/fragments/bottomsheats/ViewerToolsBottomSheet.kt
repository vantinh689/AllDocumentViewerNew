package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.BottomSheatViewerToolsBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.SharedPref
import org.koin.android.ext.android.inject
import androidx.annotation.Keep
@Keep
class ViewerToolsBottomSheet : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheatViewerToolsBinding

    private var dataModel: DataModel? = null

    private val sharedPref: SharedPref by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)

        arguments?.let {
            dataModel = it.getSerializable(Constants.DOC_MODEL) as DataModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheatViewerToolsBinding.inflate(layoutInflater)

        binding.docModel = dataModel

        initViews()
        setListeners()

        return binding.root
    }

    private fun initViews() {
        binding.nightModeSwitch.isChecked = sharedPref.getBoolean(Constants.IS_VIEWER_NIGHT_MODE)

        if (sharedPref.getBoolean(Constants.IS_VIEWER_HORIZONTAL_VIEW))
            binding.horizontalViewBtn.text = getString(R.string.vertical_view)
        else
            binding.horizontalViewBtn.text = getString(R.string.horizontal_view)
    }

    private fun setListeners() {
        binding.apply {
            view.setOnClickListener {
                dismissAllowingStateLoss()
            }
            pageByPageBtn.setOnClickListener {
                mListener?.onItemClick(PAGE_BY_PAGE)
                dismissAllowingStateLoss()
            }
            gotoBtn.setOnClickListener {
                mListener?.onItemClick(GOTO)
                dismissAllowingStateLoss()
            }
            nightModeBtn.setOnClickListener {
                nightModeSwitch.isChecked = !nightModeSwitch.isChecked
               // dismissAllowingStateLoss()
            }
            horizontalViewBtn.setOnClickListener {
                mListener?.onItemClick(IS_HORIZONTAL_VIEW)
                dismissAllowingStateLoss()
            }
            nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    sharedPref.putBoolean(Constants.IS_VIEWER_NIGHT_MODE,true)
                else
                    sharedPref.putBoolean(Constants.IS_VIEWER_NIGHT_MODE,false)
                mListener?.onItemClick(IS_NIGHT_MODE)
            }

        }
    }

    var mListener: ItemClickListener? = null

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface ItemClickListener {
        fun onItemClick(item: String)
    }

    companion object {

        const val IS_NIGHT_MODE: String = "IS_NIGHT_MODE"
        const val IS_HORIZONTAL_VIEW: String = "IS_HORIZONTAL_VIEW"
        const val PAGE_BY_PAGE: String = "PageByPage"
        const val GOTO: String = "Goto"
        const val SCAN_DOC: String = "ScanDocs"

        @JvmStatic
        fun newInstance(bundle: Bundle): ViewerToolsBottomSheet {
            val fragment = ViewerToolsBottomSheet()
            fragment.arguments = bundle
            return fragment
        }
    }
}