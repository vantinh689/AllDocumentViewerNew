package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.BottomSheatMenuBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.ModuleType
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.PdfEditorAppActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.getDrawableResource
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.launchAnotherApplication
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.openActivity
import java.util.*

class MenuBottomSheet: BottomSheetDialogFragment() {

    lateinit var binding: BottomSheatMenuBinding
    private var dataModel: DataModel? = null
    private var isFromHomeScreen: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        arguments?.let {
            dataModel = it.getSerializable(Constants.DOC_MODEL) as DataModel
            isFromHomeScreen = it.getBoolean(Constants.IS_FROM_HOME_SCREEN,true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheatMenuBinding.inflate(layoutInflater)

        binding.docModel = dataModel

        dataModel?.let {
            if (dataModel!!.isBookmarked) {
                binding.bookmarkPdf.text = getString(R.string.un_star)
                binding.bookmarkPdf.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawableResource(R.drawable.ic_un_bookmark), null, null, null)
            }else {
                binding.bookmarkPdf.text = getString(R.string.star)
                binding.bookmarkPdf.setCompoundDrawablesWithIntrinsicBounds(
                    requireContext().getDrawableResource(R.drawable.ic_bookamrk_star),
                    null,
                    null,
                    null
                )
            }
        }

        if(isFromHomeScreen == false){
            binding.rename.visibility = View.GONE
            binding.delete.visibility = View.GONE
        }

        dataModel?.type?.toLowerCase(Locale.ROOT).let { type->
            when(".$type"){
                Constants.PDF->{
                    binding.excelToPdfBtn.visibility = View.GONE
                    binding.docToPdfBtn.visibility =   View.GONE
                }
                Constants.excelExtension,Constants.excelWorkbookExtension->{
                    binding.edtBtn.visibility =        View.GONE
                    binding.reorderBtn.visibility =    View.GONE
                    binding.addRemoveBtn.visibility =  View.GONE
                    binding.compressBtn.visibility =   View.GONE
                    binding.mergeBtn.visibility =      View.GONE
                    binding.extractTxtBtn.visibility = View.GONE
                    binding.docToPdfBtn.visibility =   View.GONE
                }
                Constants.PPT,Constants.PPTX->{
                    binding.edtBtn.visibility =        View.GONE
                    binding.reorderBtn.visibility =    View.GONE
                    binding.addRemoveBtn.visibility =  View.GONE
                    binding.compressBtn.visibility =   View.GONE
                    binding.mergeBtn.visibility =      View.GONE
                    binding.extractTxtBtn.visibility = View.GONE
                    binding.excelToPdfBtn.visibility = View.GONE
                    binding.docToPdfBtn.visibility =   View.GONE
                }
                Constants.docExtension,Constants.docxExtension->{
                    binding.edtBtn.visibility =        View.GONE
                    binding.reorderBtn.visibility =    View.GONE
                    binding.addRemoveBtn.visibility =  View.GONE
                    binding.compressBtn.visibility =   View.GONE
                    binding.mergeBtn.visibility =      View.GONE
                    binding.extractTxtBtn.visibility = View.GONE
                    binding.excelToPdfBtn.visibility = View.GONE
                }
            }
        }


        setListeners()

        return binding.root
    }

    private fun setListeners(){
        binding.apply {
            view.setOnClickListener {
                dismissAllowingStateLoss()
            }
            rename.setOnClickListener {
                this@MenuBottomSheet.dataModel?.let { it1 -> mListener?.onItemClick(RENAME, it1) }
                dismissAllowingStateLoss()
            }
            share.setOnClickListener {
                this@MenuBottomSheet.dataModel?.let { it1 -> mListener?.onItemClick(SHARE, it1) }
                dismissAllowingStateLoss()
            }
            delete.setOnClickListener {
                this@MenuBottomSheet.dataModel?.let { it1 -> mListener?.onItemClick(DELETE, it1) }
                dismissAllowingStateLoss()
            }
            bookmarkPdf.setOnClickListener {
                this@MenuBottomSheet.dataModel?.let { it1 -> mListener?.onItemClick(BOOKMARK, it1) }
                dismissAllowingStateLoss()
            }
            edtBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.Edit.name)
                }
            }
            mergeBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.Merge.name)
                }
            }
            compressBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.Compress.name)
                }
            }
            reorderBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.Reorder.name)
                }
            }
            addRemoveBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.AddRemovePassword.name)
                }
            }
            extractTxtBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.ExtractText.name)
                }
            }
            excelToPdfBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.ExcelToPdf.name)
                }
            }
            docToPdfBtn.setOnClickListener {
                dismissAllowingStateLoss()
                context?.openActivity(PdfEditorAppActivity::class.java) {
                    putString(Constants.TYPE, ModuleType.DocToPdf.name)
                }
            }
        }
    }

    var mListener: ItemClickListener? = null

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    interface ItemClickListener {
        fun onItemClick(item: String, dataModel: DataModel)
    }

    companion object {
        const val RENAME: String = "RENAME"
        const val SHARE: String = "SHARE"
        const val DELETE: String = "DELETE"
        const val BOOKMARK: String = "BOOKMARK"

        @JvmStatic
        fun newInstance(bundle: Bundle): MenuBottomSheet {
            val fragment = MenuBottomSheet()
            fragment.arguments = bundle
            return fragment
        }
    }
}