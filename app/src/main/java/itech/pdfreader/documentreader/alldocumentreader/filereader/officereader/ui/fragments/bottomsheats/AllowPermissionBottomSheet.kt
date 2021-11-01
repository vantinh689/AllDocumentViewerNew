package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.annotation.Keep
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.BottomSheetAllowPermissionBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants

@Keep
class AllowPermissionBottomSheet: BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetAllowPermissionBinding

    private val TAG = AllowPermissionBottomSheet::class.java.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAllowPermissionBinding.inflate(layoutInflater)

        binding.allowBtn.setOnClickListener {
            isDismiss = false
            dismissAllowingStateLoss()
            mListener?.onItemClick(Constants.ALLOW)
        }

        binding.allowTxt.setOnClickListener {
            isDismiss = false
            dismissAllowingStateLoss()
            mListener?.onItemClick(Constants.ALLOW)
        }

        binding.cancelBtn.setOnClickListener {
            dismissAllowingStateLoss()
            mListener?.onItemClick(Constants.CANCEL)
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG,"onDismiss")
        if(isDismiss){
            mListener?.onItemClick(Constants.CANCEL)
        }
    }

    var mListener: ItemClickListener? = null
    var isDismiss = true

    interface ItemClickListener {
        fun onItemClick(item: String)
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AllowPermissionBottomSheet {
            val fragment = AllowPermissionBottomSheet()
            fragment.arguments = bundle
            return fragment
        }
    }
}