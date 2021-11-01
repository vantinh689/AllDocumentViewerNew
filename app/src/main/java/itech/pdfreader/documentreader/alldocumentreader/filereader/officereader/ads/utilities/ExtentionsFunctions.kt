package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import android.text.ClipboardManager
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.library.BuildConfig
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

object ExtentionsFunctions {

    fun Context?.hideSoftKeyboard(view: View) {
        val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun String.fromHtml(): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }
    }

    fun Activity.copyToClipboard(text: String) {
        val clipboardManager =
            getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.text = text
        Toast.makeText(applicationContext, "Copied", Toast.LENGTH_SHORT).show()
    }

    fun Context.isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    @JvmStatic
    fun convertDates(startTime: String): String? {
        var oldMillis: Long = 0
        Log.e("DB", "" + startTime)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm:ss")
        try {
            val date1 = dateFormat.parse(startTime)
            oldMillis = date1.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return getFormattedDate(oldMillis)
    }

    private fun getFormattedDate(smsTimeInMilis: Long): String? {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = smsTimeInMilis
        val now = Calendar.getInstance()
        val timeFormatString = "h:mm"
        val dateTimeFormatString = "EEEE, MMMM d, h:mm"
        val HOURS = (60 * 60 * 60).toLong()
        return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
            "Today " + DateFormat.format(timeFormatString, smsTime)
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
            "Yesterday " + DateFormat.format(timeFormatString, smsTime)
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            DateFormat.format("MMMM dd yyyy, h:mm", smsTime).toString()
        }
    }

    @JvmStatic
    fun printLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        } else {
            Timber.tag(tag).i(message)
        }
    }

    fun Context.isNetworkConnected(): Boolean {
        val mgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = mgr.activeNetworkInfo
        return netInfo != null && netInfo.isConnected && netInfo.isAvailable
    }

    fun Context.isLocationEnabled1() {
        var lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(this)
                .setMessage("Gps Network is not enabled")
                .setPositiveButton(
                    "Open Location Setting"
                ) { paramDialogInterface, paramInt ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }

    /*public fun Context.isLocationEnabled(): Boolean {
        var locationMode = 0
        val locationProviders: String
        try {
            locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        locationMode != Settings.Secure.LOCATION_MODE_OFF
    }*/

    fun Context.isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGPSEnabled || isNetworkEnabled
    }


    fun getTranslatedAyaList(xpp: XmlPullParser, position: Int, x: String): ArrayList<String> {
        val ayahList = ArrayList<String>()
        try {
            var suraFound = false
            var suraFoundandFinishedGet = false
            ayahList.add(x)
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "sura") {
                        if (xpp.getAttributeValue(0) == position.toString() + "") {
                            xpp.nextTag()
                            suraFound = true
                            suraFoundandFinishedGet = false
                            continue
                        } else {
                            if (suraFound) {
                                suraFoundandFinishedGet = true
                            }
                        }
                    }
                    if (suraFoundandFinishedGet) {
                        break
                    }
                    if (xpp.name == "aya" && suraFound) {
                        val translatedAya = xpp.getAttributeValue(1).toString()
                        translatedAya.trim { it <= ' ' }
                        ayahList.add(translatedAya)
                    }
                }
                xpp.next()
            }
        } catch (ex: Exception) {
            return ayahList
        }
        return ayahList
    }

    @JvmStatic
    fun getAudioTimeXmlParser(xpp: XmlPullParser, position: Int): ArrayList<Int> {
        val ayahList = ArrayList<Int>()
        try {
            var suraFound = false
            var suraFoundandFinishedGet = false
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "sura") {
                        if (xpp.getAttributeValue(0) == position.toString() + "") {
                            xpp.nextTag()
                            suraFound = true
                            suraFoundandFinishedGet = false
                            continue
                        } else {
                            if (suraFound) {
                                suraFoundandFinishedGet = true
                            }
                        }
                    }
                    if (suraFoundandFinishedGet) {
                        break
                    }
                    if (xpp.name == "aya" && suraFound) {
                        val time = xpp.getAttributeValue(1).toString().toInt()
                        ayahList.add(time)
                    }
                }
                xpp.next()
            }
        } catch (ex: java.lang.Exception) {
            return ayahList
        }
        return ayahList
    }

    fun getDate(): String {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        return df.format(c.time)
    }

    private const val MY_PREFS_DATE = "Date"
    fun Context.saveDate(currentDate: String = getDate()) {
        val prefs: SharedPreferences = getSharedPreferences(MY_PREFS_DATE, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("date", currentDate)
        Log.e("Saving Date in PREFS", currentDate)
        editor.apply()
    }

    fun checkifonRingerMode(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    fun Context.loadDate(): String {
        val prefs: SharedPreferences = getSharedPreferences(MY_PREFS_DATE, Context.MODE_PRIVATE)
        return if (prefs.getString("date", getDate()) == null) {
            getDate()
        } else {
            prefs.getString("date", getDate())!!
        }
    }

    @JvmStatic
    fun Context.showShortToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun Context.showLongToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun Context.cacheFile(): File {
        val sd: File = getCacheDir()
        val folder = File(sd, "/QiblaConnect/")
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                Log.e("ERROR", "Cannot create a directory!")
            } else {
                folder.mkdirs()
            }
        }
        return folder
    }

    @JvmStatic
    fun Context.hasPermissions(permissions: Array<String>?): Boolean {
        permissions?.let {
            for (permission in it) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun String.emailValidator(): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(this)
        return matcher.matches()
    }

    fun String.isValidPhoneNumber(): Boolean {
        if (this.length < 5 || this.length > 15) {
            return false
        }
        return if (!TextUtils.isEmpty(this)) {
            Patterns.PHONE.matcher(this).matches()
        } else false
    }

    fun <T> Context.moveNextActivity(next: Class<T>) {
        startActivity(Intent(this, next))
    }

    fun Context.checkDownloadManagerState(): Boolean {
        var status = false
        val state = this.packageManager.getApplicationEnabledSetting(packageName)
        status =
            !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || state == 4)
        return status
    }

    fun Context.hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun Context.loadColor(color: Int): Int {
        return ContextCompat.getColor(this, color)
    }
}