package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils


import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model.AdTypeEnum
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.printLog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object AdUtilityClass {

    fun spl(msg: String?) {
        println(msg)
    }

    fun lpl(tag: String, msg: String) {
        ExtentionsFunctions.printLog(tag, "lpl: $msg")
    }

    const val UNKNOWN_ERROR_OCCURED = "UNKNOWN_ERROR_OCCURED"
    const val UNKNOWN_ERROR_CODE = -94000

    private var interstitialAd: InterstitialAd? = null

    fun newAdMobInterstitialAd(context: Context, interstitialId: String,onAdFailedToLoad:((LoadAdError)->Unit),onAdLoaded:((InterstitialAd)->Unit),onAdClosed:((InterstitialAd)->Unit)) {
        try {
            InterstitialAd.load(context,interstitialId,AdRequest.Builder().build(),object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onAdFailedToLoad.invoke(p0)
                }

                override fun onAdLoaded(mInterstitialAd: InterstitialAd) {
                    super.onAdLoaded(mInterstitialAd)
                    onAdLoaded.invoke(mInterstitialAd)
                    mInterstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                AppOpen.isInterstitialShown = false
                                onAdClosed.invoke(mInterstitialAd)
                                Log.e("InterstiatialReload", "Reloaded___")
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                                AppOpen.isInterstitialShown = false
                                super.onAdFailedToShowFullScreenContent(p0)
                            }

                            override fun onAdShowedFullScreenContent() {
                                AppOpen.isInterstitialShown = true
                                super.onAdShowedFullScreenContent()
                            }
                        }
                    Log.e("Interstitial____", "AdLoaded____")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAdMobBanner(adMobBanner: AdView?, listner: AdListener?) {
        adMobBanner?.loadAd(AdRequest.Builder().build())
        if (listner == null)
            adMobBanner?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adMobBanner?.visibility = View.VISIBLE
                }
            }
        else adMobBanner?.adListener = listner
    }

    fun loadAdMobBanner(tag: String, context: Context, adId: String, adListener2: AdListener2? = null): AdView {
        val adView = AdView(context)
        loadAdMobBanner(tag, adId, adView, adListener2)
        return adView
    }

    fun loadAdMobBanner(
        tag: String,
        adId: String,
        adView: AdView,
        adListener2: AdListener2? = null
    ): AdView {
        if (!adView.context.isInternetConnected()) {
            adListener2?.onAdFailed("No Network", 0, AdTypeEnum.ADMOB_BANNER)
            printLog(tag, "AdFailedToLoad with code 0 ${AdTypeEnum.ADMOB_BANNER}")
            return adView
        }
        adView.adUnitId = adId
        val adRequest: AdRequest =
            AdRequest.Builder()
                .build()
        val adSize: AdSize = getAdSize(adView.context)
        adView.adSize = adSize

        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                adListener2?.onAdClicked(null, AdTypeEnum.ADMOB_BANNER)
                printLog(tag, "AdClicked ${AdTypeEnum.ADMOB_BANNER}")

            }

            override fun onAdClosed() {
                super.onAdClosed()
                adListener2?.onAdClosed(null, AdTypeEnum.ADMOB_BANNER)
                printLog(tag, "AdClosed ${AdTypeEnum.ADMOB_BANNER}")

            }

            override fun onAdFailedToLoad(p0: LoadAdError?) {
                super.onAdFailedToLoad(p0)
                adListener2?.apply {
                    val error = p0?.message ?: UNKNOWN_ERROR_OCCURED
                    val code = p0?.code ?: UNKNOWN_ERROR_CODE
                    this.onAdFailed(error, code, AdTypeEnum.ADMOB_BANNER)
                }
                printLog(tag, "AdFailedToLoad with code ${p0?.code} ${AdTypeEnum.ADMOB_BANNER}")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                adListener2?.onAdImpression()
                printLog(tag, "AdImpression ${AdTypeEnum.ADMOB_BANNER}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                adListener2?.onAdLoaded(adView, AdTypeEnum.ADMOB_BANNER)
                printLog(tag, "AdLoaded ${AdTypeEnum.ADMOB_BANNER}")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                adListener2?.onAdOpened(null, AdTypeEnum.ADMOB_BANNER)
                printLog(tag, "AdOpened ${AdTypeEnum.ADMOB_BANNER}")
            }

        }
        adView.loadAd(adRequest)
        return adView
    }

    private fun getAdSize(context: Context): AdSize {
        val display: DisplayMetrics = context.resources.displayMetrics
        val widthPixels = display.widthPixels.toFloat()
        val density = display.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    fun loadAdMobNativeAd(
        TAG: String,
        context: Context,
        adId: String,
        placement: Int,
        listener: AdListener2
    ) {
        try {
            if (!context.isInternetConnected()) {
                printLog(TAG, "AdFailedToLoad with code 0 ${AdTypeEnum.ADMOB_NATIVE}")
                return
            }
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

            val adLoader = AdLoader.Builder(context, adId)
                .forNativeAd { ad: NativeAd ->
                    listener.onAdLoaded(ad, AdTypeEnum.ADMOB_NATIVE)
                    printLog(TAG, "AdLoaded ${AdTypeEnum.ADMOB_NATIVE}")
                }
                .withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        listener?.apply {
                            val error = p0?.message ?: UNKNOWN_ERROR_OCCURED
                            val code = p0?.code ?: UNKNOWN_ERROR_CODE
                            this.onAdFailed(error, code, AdTypeEnum.ADMOB_NATIVE)
                        }
                        printLog(TAG, "AdFailedToLoad with code ${p0?.code} ${AdTypeEnum.ADMOB_NATIVE}")
                    }
                })
                .withNativeAdOptions(
                    com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                        .setMediaAspectRatio(com.google.android.gms.ads.nativead.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
                        .setAdChoicesPlacement(placement)
                        .setVideoOptions(videoOptions)
                        .build()
                )
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    val videoOptions = VideoOptions.Builder()
        .setStartMuted(true)
        .build()

    fun loadNativeAd(
        tag: String,
        context: Context,
        adId: String,
        adChoicePlacement: Int,
        listner: AdListener2
    ) {
        val adLoader1 = AdLoader.Builder(context, adId)
            .forNativeAd { ad: NativeAd ->
                listner.onAdLoaded(ad, AdTypeEnum.ADMOB_NATIVE)
                printLog(tag, "AdLoaded ${AdTypeEnum.ADMOB_NATIVE}")
            }

            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    listner.apply {
                        val error = p0?.message ?: UNKNOWN_ERROR_OCCURED
                        val code = p0?.code ?: UNKNOWN_ERROR_CODE
                        this.onAdFailed(error, code, AdTypeEnum.ADMOB_NATIVE)
                    }
                    printLog(tag, "AdFailedToLoad with code ${p0?.code} ${AdTypeEnum.ADMOB_NATIVE}")
                }
            })
            .withNativeAdOptions(
                com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                    .setMediaAspectRatio(com.google.android.gms.ads.nativead.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
                    .setAdChoicesPlacement(adChoicePlacement)
                    .setVideoOptions(videoOptions)
                    .build()
            )
            .build()
        adLoader1.loadAd(AdRequest.Builder().build())
    }

    fun loadNativeAd(
        tag: String,
        context: Context?,
        numberOfAds: Int,
        nativeAdId: String,
        adChoicePlacement: Int,
        adListener: AdListener,
        onUnifiedNativeAdLoadedListener: NativeAd.OnNativeAdLoadedListener
    ) {
        val adLoader: AdLoader = AdLoader.Builder(context, nativeAdId)
            .forNativeAd(onUnifiedNativeAdLoadedListener)
            .withAdListener(adListener)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .setAdChoicesPlacement(adChoicePlacement)
                    .build()
            )
            .build()
        adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
    }
}