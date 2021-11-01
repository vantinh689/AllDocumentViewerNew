package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils

import android.app.Activity
import android.content.Context
import android.util.Log

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.isAlreadyPurchased

open class InterstitialAdsUtils {

    var mInterstitialAd: InterstitialAd? = null
        private set

    companion object {
        @Volatile
        private var instance: InterstitialAdsUtils? = null
        var onCloseCallback: (() -> Unit)? = null
        var counter = 0
        var adShown = false
        fun getInstance() = instance ?: synchronized(this) {
                instance ?: InterstitialAdsUtils().also {
                    instance = it
                }
            }
    }


    fun loadInterstitialAd(context: Context,isFromSplash:Boolean=false) {
        if (context.isInternetConnected() && !context.isAlreadyPurchased()) {
            context.let {
                val interId = if(isFromSplash)
                    it.getString(R.string.splashInterstitialId)
                else
                    it.getString(R.string.inter_id)
                InterstitialAd.load(
                    it, interId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(ad: LoadAdError) {
                            AppOpen.isInterstitialShown = false
                            if (counter == 2) {
                                onCloseCallback?.invoke()
                            } else {
                                counter++
                                onCloseCallback?.invoke()
                                loadInterstitialAd(context)
                            }
                            adShown = false
                        }
                        override fun onAdLoaded(ad: InterstitialAd) {
                            mInterstitialAd = ad
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        onCloseCallback?.invoke()
                                        AppOpen.isInterstitialShown = false
                                        mInterstitialAd = null
                                        loadInterstitialAd(context)
                                        Log.e("InterstiatialReload", "Reloaded___")
                                        adShown = false
                                    }
                                    override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                                        AppOpen.isInterstitialShown = false
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        onCloseCallback?.invoke()
                                        adShown = false
                                    }
                                    override fun onAdShowedFullScreenContent() {
                                        AppOpen.isInterstitialShown = true
                                        super.onAdShowedFullScreenContent()
                                        adShown = true
                                    }
                                }
                            Log.e("Interstitial____", "AdLoaded____")
                        }
                    })
            }
        }
    }

    // to show Interstitial Ad Activity reference must be given
    fun showInterstitialAdNew(activity: Activity, onAction: (() -> Unit)? = null) {
        if(!activity.isAlreadyPurchased()) {
            if (mInterstitialAd != null) {
                if (!adShown) {
                    activity.let { mInterstitialAd?.show(it) }
                }
                onCloseCallback = {
                    onAction?.invoke()
                    onCloseCallback = null
                }
            } else {
                loadInterstitialAd(activity)
                onAction?.invoke()
            }
        }else{
            onAction?.invoke()
        }
    }
}