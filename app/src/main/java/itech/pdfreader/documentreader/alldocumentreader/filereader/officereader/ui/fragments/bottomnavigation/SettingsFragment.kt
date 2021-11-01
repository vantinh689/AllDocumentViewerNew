package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentSettingsBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DashboardActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.RateDialogNew
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.BaseFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*

class SettingsFragment : BaseFragment() {

    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        binding.privacyBtn.setOnClickListener {
            requireActivity().openUrl("https://itechsolutionapps.wordpress.com/")
        }
        binding.rateUsBtn.setOnClickListener {
            RateDialogNew(requireActivity()).createRateUsDialog(false,sharedPref)
        }
        binding.shareBtn.setOnClickListener {
            requireActivity().shareApp()
        }
        binding.restorePurchaseBtn.setOnClickListener {
            binding.root.snack("Coming soon")
            //requireActivity().openActivity(SubscriptionActivity::class.java)
        }

        refreshAdOnView()

        return binding.root
    }

    private val TAG = "SettingsFragment"

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()

        if (!utilsViewModel.isPremiumUser() && requireContext().isInternetConnected()) {
            binding.adLayout.visibility = View.VISIBLE
            requireActivity().setNativeAd(
                utilsViewModel.isPremiumUser(),
                binding.adLayout,
                R.layout.empty_screen_ad_layout,
                TAG,
                placement = NativeAdOptions.ADCHOICES_BOTTOM_RIGHT,
                preLoadedNativeAd = nativeAd,
                adMobNativeId = getString(R.string.native_id), onFailed = {
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.adLayout.visibility = View.GONE
                    binding.shimmerViewContainer.stopShimmer()
                }
            ) {
                nativeAd = it as NativeAd
                binding.shimmerViewContainer.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
    }

    companion object {
        var nativeAd: NativeAd? = null
    }
}