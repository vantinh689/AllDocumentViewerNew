package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.DialogExitBottomSheatBinding
import androidx.annotation.Keep
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DashboardActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.preLoadedNativeAd

@Keep
class ExitBottomSheetDialog: BottomSheetDialogFragment() {

    lateinit var binding: DialogExitBottomSheatBinding

//    val splashViewModel: SplashViewModel by viewModel()
    private val TAG = ExitBottomSheetDialog::class.java.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogExitBottomSheatBinding.inflate(layoutInflater)

        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        refreshAd()

        binding.exitBtn.setOnClickListener {
            dismissAllowingStateLoss()
            activity?.finishAffinity()
        }
        binding.CancelBtn.setOnClickListener {
            dismissAllowingStateLoss()
            mListener?.onItemClick("Cancel")
        }

        return binding.root
    }

    private fun refreshAd() {
        /**show native ad*/
        if (!(requireActivity() as DashboardActivity).utilsViewModel.isPremiumUser() && requireContext().isInternetConnected()) {
            binding.adLayout.visibility = View.VISIBLE
            requireActivity().setNativeAd(
                (requireActivity() as DashboardActivity).utilsViewModel.isPremiumUser(),
                binding.adLayout,
                R.layout.exit_bottom_sheet_native_ad_layout,
                TAG,
                preLoadedNativeAd =preLoadedNativeAd,
                adMobNativeId = getString(R.string.native_id),
                onFailed = {
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.adLayout.visibility = View.GONE
                    binding.shimmerViewContainer.stopShimmer()
                }
            ) {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
    }

    private var mListener: ItemClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (context is ItemClickListener) {
//            mListener = context
//        } else {
//            throw RuntimeException(
//                "$context must implement ItemClickListener"
//            )
//        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    interface ItemClickListener {
        fun onItemClick(item: String)
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ExitBottomSheetDialog {
            val fragment = ExitBottomSheetDialog()
            fragment.arguments = bundle
            return fragment
        }
    }
}