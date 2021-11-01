package com.reader.office.mychanges.slidernativead

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.reader.office.R
import com.reader.office.getNativeAdObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class NativeAdsViewHolderClass(
    val context: Context,
    view: View,
    var nativeAdId: Int,
    var callback: NativeAdCallback
) : RecyclerView.ViewHolder(view) {

    private var unifiedNativeAd = view.findViewById<NativeAdView>(R.id.uniform)
    private var nativeAdCardView = view.findViewById<CardView>(R.id.nativAdCardView)
    var layoutShimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)

    var mNativeAd: NativeAd? = null
    fun setData(nativeAd: Any?, position: Int, priority: Int) {

        if (nativeAd == null) {
            showShimmer()
            unifiedNativeAd.visibility = View.GONE

            Log.d("List_nativee", "Request sent for position $position, Type $priority")
            val nativeAdPlacement = com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
            GlobalScope.launch(Dispatchers.IO) {
                itemView.context?.getNativeAdObject("ListNativeItem $position",
                    nativeAdPlacement,
                    itemView.context.getString(nativeAdId),periority = priority,
                    onResult = { nativeAd ->
                        GlobalScope.launch(Dispatchers.Main) {
                            nativeAd.let {
                                if (nativeAd == null) {
//                                admobContainer?.visibility = View.GONE
                                } else {
                                    callback.onNewAdLoaded(it!!, position)
                                    dismissShimmer()
                                    if (it is NativeAd) {
                                        mNativeAd = it
                                        unifiedNativeAd.post {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                GlobalScope.launch(Dispatchers.Main) {
                                                    populateUnifiedNativeAdView(
                                                        mNativeAd!!,
                                                        unifiedNativeAd
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },bannerId = itemView.context.getString(R.string.admobBanner),onAdClicked = {

                    })
            }
        } else {

            GlobalScope.launch(Dispatchers.IO) {
                nativeAd.let { it ->
                    when (it) {
                        is NativeAd -> {
//                            callback.onNewAdLoaded(it, position)
                            GlobalScope.launch(Dispatchers.Main) {
                                mNativeAd?.let {
                                    dismissShimmer()
                                    populateUnifiedNativeAdView(it, unifiedNativeAd)
                                }
                            }
                        }
                        else -> {
                            dismissShimmer()
                        }
                    }
                }

            }
        }
    }

    fun Context.isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun showShimmer() {
        if(context.isInternetConnected()) {
            try {
                layoutShimmer.startShimmer()
                layoutShimmer.visibility = View.VISIBLE
            } catch (e: Exception) { }
        }else
            dismissShimmer()
    }

    private fun dismissShimmer() {
        try {
            layoutShimmer.stopShimmer()
            layoutShimmer.visibility = View.GONE
        } catch (e: Exception) { }
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView =
            adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)
        adView.mediaView = mediaView
        mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                try {
                    if (child is ImageView) {
                        child.adjustViewBounds = true
                        child.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                } catch (e: Exception) {
                }
            }

            override fun onChildViewRemoved(parent: View, child: View) {}
        })

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.tvActionBtnTitle)

        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            if (adView.callToActionView is TextView)
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            if (adView.callToActionView is TextView) {
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            }
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            Glide.with(adView.context.applicationContext)
                .load(nativeAd.icon?.drawable)
                .into((adView.iconView as ImageView))
            adView.iconView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
        unifiedNativeAd.visibility = View.VISIBLE
        nativeAdCardView.visibility = View.VISIBLE
    }

}