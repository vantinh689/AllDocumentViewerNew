package com.reader.office.mychanges.slidernativead

interface NativeAdCallback {
    fun onNewAdLoaded(nativeAd: Any, position: Int)
    fun onAdClicked(position: Int)
}