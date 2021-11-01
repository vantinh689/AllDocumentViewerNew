package com.reader.office.wp

interface AdsListenerNative {
    fun onAdLoaded(ad:Any? , type: AdType)
    fun onAdClosed(ad: Any?,type: AdType)
    fun onAdFailed(error:String,extraCode:Int , type: AdType)
    fun onAdImpression(){}
    fun onAdLeftApplication(ad:Any?,type: AdType){}
    fun onAdClicked(ad:Any?,type: AdType){}
    fun onAdOpened(ad:Any?,type: AdType){}
}