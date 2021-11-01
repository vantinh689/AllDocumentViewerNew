package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model.AdTypeEnum
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.hasInternetConnection
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.printLog
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isNetworkConnected


fun Context.getInterstitialAdObject(
    periority: Int,
    tag: String,
    admobId: String,
    onResult: (Any?) -> Unit,
    onAdClosed: (Any?) -> Unit,
    onFailed: (String) -> Unit,
) {
    val context = this
    if (!context.isInternetConnected()) {
        onResult(null)
        return
    }

    when (periority) {
        2 -> {
            AdUtilityClass.newAdMobInterstitialAd(this, admobId, onAdLoaded = {
                printLog("int_status$tag", "Native Interstitial Loaded Successfully")
                onResult(it)
            }, onAdFailedToLoad = {
                printLog("int_status$tag", "Admob Interstitial loading Fialedd, FB Request sent")
                onFailed(it.message)
            }, onAdClosed = {
                printLog("int_status$tag", "Interstitial Closed")
                onAdClosed(it)
            })

        }
    }
}

//fun Context.getInterstitialAdObject(
//    showFullScreenAds: InterstitialAdUpdated,
//    context: Context,
//    periority: Int,
//    onResult: (Any?) -> Unit,
//) {
//    if (!isInternetConnected()) {
//        onResult(null)
//        return
//    }
//    when (periority) {
//        0 -> {
//            showFullScreenAds.loadInterstitialAd(context,onAdLoadedListner = { onResult.invoke(it) })
//        }
//    }
//}

private var adFailedFbAdCounter = 0

fun Context.getNativeAdObject(
    tag: String, placement: Int, admobId: String,
    periority: Int, onResult: (Any?) -> Unit
) {
    val context = this
    if (!context.hasInternetConnection()) {
        onResult(null)
        return
    }
    when (periority) {
        2 -> {
            AdUtilityClass.loadNativeAd(
                tag,
                context,
                admobId,
                placement,
                object : AdListener2 {
                    override fun onAdLoaded(ad: Any?, typeEnum: AdTypeEnum) {
                        printLog("$tag native_status", "Priority $periority Admob Native Ad Loaded Successfully")
                        onResult(ad)
                    }

                    override fun onAdClosed(ad: Any?, typeEnum: AdTypeEnum) {
                        onResult(ad)
                    }

                    override fun onAdFailed(error: String?, extraCode: Int?, typeEnum: AdTypeEnum) {
                        printLog(
                            "$tag native_status",
                            "Priority $periority Native Ad Failed and FB Native Request Sent"
                        )
                    }
                })
        }
    }
}

fun Activity.loadInterstitialAd(
    isPremiumUser: Boolean,
    TAG: String? = null,
    adMobInterId: String? = null,
    onResult: ((Any?) -> Unit)? = null,
    onClose: ((Any?) -> Unit)? = null,
    onFailed: ((String?) -> Unit)? = null,
) {
    if (isNetworkConnected() && !isPremiumUser) {
        getInterstitialAdObject(
            2,
            TAG + "_Interstitial",
            adMobInterId ?: getString(R.string.inter_id),
            onResult = { interstitial ->
                onResult?.invoke(interstitial)
            },
            onAdClosed = {
                onClose?.invoke(it)
            }, onFailed = {
                onFailed?.invoke(it)
            })
    } else
        onFailed?.invoke("")
}

fun Activity.showLoadedInterstitialAd(interstitialAd: Any?, onFailed: (() -> Unit)? = null) {
    if (interstitialAd != null) {
        if (interstitialAd is InterstitialAd) {
            interstitialAd.show(this)
        } else {
            onFailed?.invoke()
        }
    } else {
        onFailed?.invoke()
    }
}

fun Activity.refreshPreLoadedNativeAd(
    isPremiumUser: Boolean,
    TAG: String,
    onResult: ((Any?) -> Unit)? = null
) {
    if (isNetworkConnected() && !isPremiumUser) {
        val placement = com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
        getNativeAdObject(TAG + "_Native", placement,
            admobId = getString(R.string.native_id),
            periority = 2,
            onResult = { nativeAdO ->
                onResult?.invoke(nativeAdO)
            })
    }
}

// to show Interstitial Ad Activity reference must be given
//fun Activity.showInterstitialAdNew(onAdDismissedListner: ((Any) -> Unit?)? = null) {
//    if (!Companions.isPurchased) {
//        mInterstitialAd?.fullScreenContentCallback =
//            object : FullScreenContentCallback() {
//                override fun onAdDismissedFullScreenContent() {
//                    Log.e("AdInterstitial", "onAdDismissedFullScreenContent")
//                    mInterstitialAd = null
//                    isAdLoaded = false
//                    onAdDismissedListner?.invoke("dismiss")
//                    OpenApp.isInterstitialShown = false
//                    if (!Companions.isPurchased) {
//                        loadInterstitialAd(this)
//                    }
//                }
//                override fun onAdImpression() {
//                    OpenApp.isInterstitialShown = true
//                    super.onAdImpression()
//                }
//                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
//                    OpenApp.isInterstitialShown = false
//                    super.onAdFailedToShowFullScreenContent(p0)
//                }
//            }
//        if (isAdLoaded) {
//            mInterstitialAd?.show(activity)
//        }
//    }
//}

fun Activity.setNativeAd(
    isPremiumUser: Boolean,
    adFrame: FrameLayout,
    nativeAdScreen: Int,
    TAG: String,
    placement: Int? = null,
    preLoadedNativeAd: Any? = null,
    adMobNativeId: String? = null,
    onFailed: (() -> Unit)? = null,
    onResult: ((Any) -> Unit)? = null,
) {
    if (isNetworkConnected() && !isPremiumUser) {
        var p2 = placement
        if (placement == null)
            p2 = NativeAdOptions.ADCHOICES_TOP_LEFT
        if (preLoadedNativeAd == null) {
            p2?.let {
                getNativeAdObject(TAG + "_Native", it,
                    admobId = adMobNativeId ?: getString(R.string.native_id),
                    periority = 2,
                    onResult = { nativeAdO ->
                        nativeAdO?.let {
                            if (it is NativeAd) {
                                NativeAdsHelperClass(this).setLoadedNativeAd(adFrame, nativeAdScreen, it)
                                onResult?.invoke(nativeAdO)
                            }
                        }
                    })
            }
        } else {
            preLoadedNativeAd.let {
                if (it is NativeAd && !isPremiumUser) {
                    NativeAdsHelperClass(this).setLoadedNativeAd(
                        adFrame,
                        nativeAdScreen,
                        it
                    )
                    onResult?.invoke(it)
                }
            }
        }
    } else {
        onFailed?.invoke()
    }
}







