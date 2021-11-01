package com.reader.office.wp

import android.content.Context
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd

object AdsUtility {
    val videoOptions = VideoOptions.Builder()
        .setStartMuted(true)
        .build()
    const val UNKNOWN_ERROR_OCCURED = "UNKNOWN_ERROR_OCCURED"
    const val UNKNOWN_ERROR_CODE = -94000
    fun loadNativeAd(
        tag: String,
        context: Context,
        adId: String,
        adChoicePlacement: Int,
        listner: AdsListenerNative) {
        val adLoader1 = AdLoader.Builder(context, adId)
            .forNativeAd { ad: NativeAd ->
                listner.onAdLoaded(ad, AdType.ADMOB_NATIVE)
                println("AdLoaded ${AdType.ADMOB_NATIVE}")
            }

            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    listner?.apply {
                        val error = p0?.message ?: AdsUtility.UNKNOWN_ERROR_OCCURED
                        val code = p0?.code ?: AdsUtility.UNKNOWN_ERROR_CODE
                        this.onAdFailed(error, code, AdType.ADMOB_NATIVE)
                    }

                    println( "AdFailedToLoad with code ${p0?.code} ${AdType.ADMOB_NATIVE}")
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
}