package com.reader.office.mychanges.bottomsheetfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reader.office.R
import com.reader.office.databinding.BottomSheatViewerTools2Binding
import com.reader.office.mychanges.utils.DOC_MODEL
import com.reader.office.mychanges.model.DataModel2

class ViewerToolsBottomSheet2 : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheatViewerTools2Binding

    private var dataModel: DataModel2? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)

        arguments?.let {
            dataModel = it.getSerializable(DOC_MODEL) as DataModel2
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheatViewerTools2Binding.inflate(layoutInflater)

        binding.docModel = dataModel

        initViews()
        setListeners()

        return binding.root
    }

    private fun initViews() {
//        dataModel?.let {
//            if (dataModel!!.isBookmarked) {
//                binding.bookamrk.text = "Unstar"
//                binding.bookamrk.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources, R.drawable.ic_un_bookmark, context?.theme), null, null, null)
//            }else {
//                binding.bookamrk.text = "Star"
//                binding.bookamrk.setCompoundDrawablesWithIntrinsicBounds(
//                    ResourcesCompat.getDrawable(resources, R.drawable.ic_bookamrk_star, context?.theme),
//                    null,
//                    null,
//                    null
//                )
//            }
//        }
    }

    private fun setListeners() {
        binding.apply {
            view.setOnClickListener {
                dismissAllowingStateLoss()
            }
            shareBtn.setOnClickListener {
                mListener?.onItemClick(SHARE)
                dismissAllowingStateLoss()
            }
            rename.setOnClickListener {
                mListener?.onItemClick(RENAME)
                dismissAllowingStateLoss()
            }
            bookamrk.setOnClickListener {
                mListener?.onItemClick(BOOKMARK)
                dismissAllowingStateLoss()
            }
            delete.setOnClickListener {
                mListener?.onItemClick(DELETE)
                dismissAllowingStateLoss()
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
        const val SHARE: String = "SAHRE"
        const val RENAME: String = "RENAME"
        const val DELETE: String = "DELETE"
        const val BOOKMARK: String = "BOOKMARK"
        @JvmStatic
        fun newInstance(bundle: Bundle): ViewerToolsBottomSheet2 {
            val fragment = ViewerToolsBottomSheet2()
            fragment.arguments = bundle
            return fragment
        }
    }
}