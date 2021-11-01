package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivitySplashBinding
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.MobileAds
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
//import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.billing.BillingViewModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isPurchased

@Keep
class SplashScreenActivity : BaseActivity() {
    lateinit var binding: ActivitySplashBinding
    private val TAG = "SplashActivity"

//    private val billingViewModel: BillingViewModel by viewModel()

    private var isSplashTimerComplete = false
    private var isFileLoadingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)

        initApplication()

        setOnClickListeners()
        setContentView(binding.root)
    }

    private fun initApplication() {
        hideStatusBar()
//        billingViewModel.subSkuDetailsListLiveData.observe(this, { skuList ->
//            if (!skuList.isNullOrEmpty()) {
//                if (!skuList[0].canPurchase || !skuList[1].canPurchase) {
//                    utilsViewModel.setPremiumUser(true)
//                    utilsViewModel.setAutoAdsRemoved(true)
//                    isPurchased = true
//                } else
//                    initNotPurchase()
//            }
//        })

        createMyFilesDirs()

        if (utilsViewModel.checkPermission(this)) {
            dataViewModel.retrieveFilesFormDevice {
                isFileLoadingComplete = true
                if (sharedPref.getBoolean(Constants.isGetStartedBtnClick)) {
                    navigateToNextScreen()
                } else {
                    binding.loadingTxt.visibility = View.GONE
                    binding.getStartBtn.visibility = View.VISIBLE
                }
            }
        } else
            isFileLoadingComplete = true

        initNotPurchase()
    }

    private fun initNotPurchase() {
        utilsViewModel.setPremiumUser(false)
        utilsViewModel.setAutoAdsRemoved(false)
        isPurchased = false
        MobileAds.initialize(this)
        InterstitialAdsUtils.getInstance().loadInterstitialAd(this@SplashScreenActivity, true)
        utilsViewModel.syncRemoteConfig()
        refreshAD()

    }

    override fun onResume() {
        super.onResume()
        applySplashTimer()
    }

    private fun refreshAD() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        setNativeAd(
            utilsViewModel.isPremiumUser(),
            binding.adFrame,
            R.layout.splash_native_ad,
            TAG,
            adMobNativeId = getString(R.string.splashScreenNativeId), onFailed = {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.adLayout.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
            }
        ) {
            binding.shimmerViewContainer.visibility = View.GONE
            binding.shimmerViewContainer.stopShimmer()
        }
    }


    private fun applySplashTimer() {
        if (isInternetConnected()) {
            if (!utilsViewModel.isPremiumUser()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    isSplashTimerComplete = true
                    if (sharedPref.getBoolean(Constants.isGetStartedBtnClick)) {
                            navigateToNextScreen()
                    } else {
                        binding.loadingTxt.visibility = View.GONE
                        binding.getStartBtn.visibility = View.VISIBLE
                    }
                }, 6000)
            } else
                startWithTwoSecondsDelay()
        } else {
            startWithTwoSecondsDelay()
        }
    }

    private fun startWithTwoSecondsDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            isSplashTimerComplete = true
            if (sharedPref.getBoolean(Constants.isGetStartedBtnClick)) {
                navigateToNextScreen()
            } else {
                binding.loadingTxt.visibility = View.GONE
                binding.getStartBtn.visibility = View.VISIBLE
            }
        }, 2000)
    }


    private fun setOnClickListeners() {
        binding.getStartBtn.setOnClickListener {
            sharedPref.putBoolean(Constants.isGetStartedBtnClick, true)
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        if (isFileLoadingComplete && isSplashTimerComplete) {
            InterstitialAdsUtils.getInstance().showInterstitialAdNew(this) {
                checkPermissionAndNavigateToMainActivity()
            }
        }
    }

    private fun checkPermissionAndNavigateToMainActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!utilsViewModel.checkPermission(this))
                openActivityWithClearTask(PermissionActivity::class.java)
            else
                openActivityWithClearTask(DashboardActivity::class.java)
        } else {
            openActivityWithClearTask(PermissionActivity::class.java)
        }
    }
}