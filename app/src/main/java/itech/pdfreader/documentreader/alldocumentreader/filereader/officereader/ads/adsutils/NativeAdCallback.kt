package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils

interface NativeAdCallback {
    fun onNewAdLoaded(nativeAd: Any, position: Int)
    fun onAdClicked(position: Int)
}