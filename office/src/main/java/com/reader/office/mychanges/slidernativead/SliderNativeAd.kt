package com.reader.office.mychanges.slidernativead

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class SliderNativeAd(val context: Context, val adLayout: RecyclerView) {
    companion object {
        const val NATIVE_AD = "nativeAD"
    }
    var index = 0
    var sliderDelay = 4000L
    var timerRunnableAd: Runnable? = null
    var timerHandlerAd: Handler? = null
    fun setAutoScrollRecyclerView() {
        timerHandlerAd = Handler(Looper.myLooper()!!)
        object : Runnable {
            override fun run() {
                timerHandlerAd?.removeCallbacks(this)
                if (index <= adsList.size - 1) {
                    adLayout.smoothScrollToPosition(index)
                    index++
                    timerHandlerAd?.postDelayed(this, sliderDelay)
                } else {
                    index = 0
                    adLayout.smoothScrollToPosition(index)
                    index++
                    timerHandlerAd?.postDelayed(this, sliderDelay)
                }
            }
        }.also { timerRunnableAd = it }
        timerHandlerAd?.postDelayed(timerRunnableAd as Runnable, sliderDelay)
    }

    fun Context.isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting ?: false
    }

    private var adsList = mutableListOf<AdModelClass>()
    fun refreshAd(crossPromotionRemoteList: MutableList<AppsLinks>) {
        /**show native ad*/
        if (context.isInternetConnected()) {
            adsList.add(AdModelClass("", "", "", "", "", NATIVE_AD))
            if (!crossPromotionRemoteList.isNullOrEmpty()) {
                Log.d(TAG, "refreshAd: " + crossPromotionRemoteList.toString())
                crossPromotionRemoteList.forEach {
                    Log.d(TAG, "refreshAd: " + it.appCoverLink)
                    adsList.add(
                        AdModelClass(
                            it.appShortDesc.toString(),
                            it.appShortDesc.toString(), "INSTALL",
                            it.appCoverLink.toString(),
                            it.appIconLink.toString(),
                            it.appLink.toString()
                        )
                    )
                }
            }
            else {
                adsList.add(
                    AdModelClass(
                        "PDF Reader-PDF Editor, Creator",
                        "iTech Solution AppsProductivity",
                        "INSTALL",
                        "https://play-lh.googleusercontent.com/2Ax7coEMZiUOqtyyzIgOXbZm9V9Js8dcfsaNZTs2dXRIm7alecD9m7iyf_ZzSgnjS8WE=s180-rw",
                        "https://play-lh.googleusercontent.com/2Ax7coEMZiUOqtyyzIgOXbZm9V9Js8dcfsaNZTs2dXRIm7alecD9m7iyf_ZzSgnjS8WE=s180-rw",
                        "https://play.google.com/store/apps/details?id=itech.pdfreader.documentreader.alldocumentreader.filereader.officereader"
                    )
                )
            }
        }
        val adapterScroller = ScrollerAdsAdapter(adsList)
        adLayout.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL)
        adLayout.adapter = adapterScroller
        adapterScroller.setOnItemClickListener {
            if (it.adType != NATIVE_AD)
                context.openUrl(it.adType)
        }
        /************************/
    }

    fun Context.openUrl(url: String) {

        try {
            val uri = Uri.parse(url) // missing 'http://' will cause crashed
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this, "No application can handle this request."
                        + " Please install a webbrowser", Toast.LENGTH_LONG
            ).show();
            e.printStackTrace();
        }
    }

    private val TAG = "SliderNativeAd"
}

