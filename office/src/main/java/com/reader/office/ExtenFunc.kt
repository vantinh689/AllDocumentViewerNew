package com.reader.office

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reader.office.R
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.reader.office.wp.AdType
import com.reader.office.wp.AdsListenerNative
import com.reader.office.wp.AdsUtility
import com.reader.office.wp.AdsUtility.loadNativeAd
import com.reader.office.wp.RoundedImageView
import kotlin.math.roundToInt


fun Activity.copyUriToExternalFilesDir(uri: Uri, fileName: String) {
    try {
        val inputStream = contentResolver.openInputStream(uri)
        val tempDir = externalCacheDir.toString()
        if (inputStream != null) {
            val file = File("$tempDir/$fileName")
            val fos = FileOutputStream(file)
            val bis = BufferedInputStream(inputStream)
            val bos = BufferedOutputStream(fos)
            val byteArray = ByteArray(1024)
            var bytes = bis.read(byteArray)
            while (bytes > 0) {
                bos.write(byteArray, 0, bytes)
                bos.flush()
                bytes = bis.read(byteArray)
            }
            bos.close()
            fos.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()

    }
}


fun Activity.getFileNameByUri(uri: Uri): String {
    var fileName = System.currentTimeMillis().toString()
    val cursor = contentResolver.query(uri, null, null, null, null)
    if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        fileName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
        cursor.close()
    }
    return fileName
}
fun Context.showNativeAds(shimmer_view_container: ShimmerFrameLayout, layoutNativeContainer: FrameLayout) {
    shimmer_view_container.startShimmer()
    layoutNativeContainer.visibility = View.VISIBLE
    val placement = com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT
    getNativeAdObject("SplashScreenNative",
        placement,
        admobId = getString(R.string.admobNative),
        bannerId = "",
        periority = 0,
        onResult = { nativeAdO ->
            shimmer_view_container?.stopShimmer()
            shimmer_view_container.visibility=View.GONE
            nativeAdO?.let {
                if (it is NativeAd) {
                    val adView = LayoutInflater.from(applicationContext).inflate(R.layout.native_ad_layout, null) as NativeAdView
                    layoutNativeContainer?.let { adContainer ->
                        populateAdmobNativeAdView(it, adView, adContainer)
                        adContainer.visibility = View.VISIBLE

                    }
                }else{
                    layoutNativeContainer.visibility=View.GONE

/*
                    loadNativeAd(applicationContext,shimmer_view_container,layoutNativeContainer,next,native_ad_container,adChoicesContainer,resources.getString(R.string.facebook_native_splash),progressBar)
*/

                }
            }
        },
        onAdClicked = {

        })
}


fun populateAdmobNativeAdView(
    nativeAd: NativeAd,
    adView: NativeAdView,
    nativeContainer: FrameLayout
) {

    nativeContainer.removeAllViews()
    nativeContainer.addView(adView)

    val mediaView: MediaView = adView.findViewById(R.id.ad_media)

    mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View, child: View) {
            try {
                if (child is ImageView) {
                    val imageView = child
                    imageView.adjustViewBounds = true
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
            }
        }

        override fun onChildViewRemoved(parent: View, child: View) {}
    })
    adView.mediaView = mediaView

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.advertiserView = adView.findViewById(R.id.ad_body)

    // The headline is guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView.visibility = View.VISIBLE
    } else {
        adView.bodyView.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }
    if (nativeAd.starRating==null){
        adView.starRatingView.visibility= View.INVISIBLE
    }else{
        (adView.starRatingView as RatingBar).numStars= nativeAd.starRating.roundToInt()
    }
    if (nativeAd.callToAction == null) {
        adView.callToActionView.visibility = View.INVISIBLE
    } else {
        adView.callToActionView.visibility = View.VISIBLE
        (adView.callToActionView as TextView).text = nativeAd.callToAction
    }
    if (nativeAd.icon == null) {
        adView.iconView.visibility = View.GONE
    } else {
        (adView.iconView as RoundedImageView).setImageDrawable(nativeAd.icon.drawable)
        adView.iconView.visibility = View.VISIBLE
    }
    if (nativeAd.advertiser == null) {
        adView.advertiserView.visibility = View.VISIBLE
    } else {
        (adView.advertiserView as TextView).text = nativeAd.advertiser
        adView.advertiserView.visibility = View.VISIBLE
    }
    adView.setNativeAd(nativeAd)
}
fun Context.getNativeAdObject(tag: String, placement: Int, admobId: String, bannerId: String,
                              periority: Int, onResult: (Any?) -> Unit, onAdClicked: () -> Unit) {
    val context = this
    if (!context.isNetworkConnected()) {
        onResult(null)
        return
    }



    AdsUtility.loadNativeAd(tag, context,  admobId, com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_LEFT, object :
        AdsListenerNative {
        override fun onAdLoaded(ad: Any?, type: AdType) {
            println("$tag native_status Priority $periority Native Ad Loaded")
            onResult(ad)

        }

        override fun onAdClosed(ad: Any?, type: AdType) {
            onResult(ad)
        }

        override fun onAdFailed(error: String, extraCode: Int, type: AdType) {


            println("$tag native_status Priority $periority Native Ad Failed and Banner Request Sent")
        }
    })


}
fun Context.isNetworkConnected(): Boolean {
    var result = false
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run { cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    result = true
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    result = true
                }
            }
        }
    }
    return result
}

@BindingAdapter("setImageSrc")
fun loadImage(imageView: ImageView, url: String) {
    val circularProgressDrawable = CircularProgressDrawable(imageView.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(imageView)
        .load(url)
        .placeholder(circularProgressDrawable)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .into(imageView)
}