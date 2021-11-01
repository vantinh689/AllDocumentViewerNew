package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.App
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.view.AppOpenLoaderScreen
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.SplashScreenActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.isAlreadyPurchased
import org.jetbrains.annotations.NotNull
import timber.log.Timber

class AppOpen(private val globalClass: App) : Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private val log = "AppOpenManager"
    private var adVisible=false
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var myApplication: App? = globalClass
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    private var appOpenLoaderScreen: AppOpenLoaderScreen? = null
    companion object {
        var COUNTER = 1
        var isInterstitialShown = false
    }
    init {
        this.myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    /**
     * Request an ad
     */
    fun fetchAd() {
        Timber.d("fetchAd %s", isAdAvailable())
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return
        }
        /*
          Called when an app open ad has failed to load.
          @param loadAdError the error.
        */
        // Handle the error.
        val loadCallback: AppOpenAdLoadCallback = object : AppOpenAdLoadCallback() {
//            /**
//             * Called when an app open ad has loaded.
//             *
//             * @param ad the loaded app open ad.
//             */
//            override fun onAppOpenAdLoaded(ad: AppOpenAd) {
//                appOpenAd = ad
//                Timber.d("loaded")
//            }
//            /**
//             * Called when an app open ad has failed to load.
//             *
//             * @param loadAdError the error.
//             */
//            override fun onAppOpenAdFailedToLoad(loadAdError: LoadAdError) {
//                // Handle the error.
//                Timber.d("error")
//            }

            /**
             * Called when an app open ad has loaded.
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                Timber.d("loaded")
                super.onAdLoaded(ad)
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(p0: LoadAdError) {
                // Handle the error.
                Timber.d("error")
                super.onAdFailedToLoad(p0)
            }
        }
        val request: AdRequest = getAdRequest()
        AppOpenAd.load(
            myApplication,
            globalClass.getString(R.string.admob_app_open_id),
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback
        )
    }
    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        COUNTER++
        if (!isShowingAd && isAdAvailable() && !isInterstitialShown) {
            Timber.d("Will show ad.")
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                    dismissLoading(1)
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    dismissLoading(3)
                }
                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
//            if (COUNTER % 2 == 0) {
            adVisible = true
            showLoading()
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }
//            } else {
//                dismissLoading(2)
//            }
        } else {
            Log.d(log, "Can not show ad.")
            dismissLoading(4)
            fetchAd()
        }
    }
    private fun showLoading() {
        if (!currentActivity?.isFinishing!!) {
            appOpenLoaderScreen = AppOpenLoaderScreen(currentActivity!!)
            appOpenLoaderScreen?.show()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        //App in background
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
       // Companions.bitmapSticker?.recycle()
        currentActivity?.let {
            if (it !is SplashScreenActivity) {
                if (!it.isAlreadyPurchased())
                    showAdIfAvailable()
            }
        }
        // App in foreground
    }
    private fun dismissLoading(from:Int) {
        Log.d("dismiss55","11$from")
        if(!adVisible) {
            try {
                appOpenLoaderScreen?.dismiss()
            } catch (e: Exception) {
            }
        }
    }
    /**
     * Creates and returns ad request.
     */
    @NotNull
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }
    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}