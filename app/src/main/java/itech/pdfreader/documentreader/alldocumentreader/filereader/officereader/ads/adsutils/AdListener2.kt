package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils

import android.util.Log
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model.AdTypeEnum

interface AdListener2 {
    fun onAdLoaded(ad:Any?, typeEnum: AdTypeEnum)
    fun onAdClosed(ad: Any?, typeEnum: AdTypeEnum)
    fun onAdFailed(error:String?, extraCode:Int?, typeEnum: AdTypeEnum)
    fun onAdImpression(){}
    fun onAdLeftApplication(ad:Any?, typeEnum: AdTypeEnum){}
    fun onAdClicked(ad:Any?, typeEnum: AdTypeEnum){}
    fun onAdOpened(ad:Any?, typeEnum: AdTypeEnum){
        Log.d("type","yhjkl")
    }
}