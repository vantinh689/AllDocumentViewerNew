package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import android.Manifest
import android.R.attr.targetSdkVersion
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.shockwave.pdfium.PdfiumCore
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.BuildConfig
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.PdfDocumentAdapter
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.collections.ArrayList

fun View.isVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    val snack = Snackbar.make(this, message, duration)
    snack.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, context.theme))
    snack.show()
}

fun Context.showToast(msg: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}

//fun Context.isPermissionsGranted() = REQUIRED_PERMISSIONS_Gallery.all {
//    ContextCompat.checkSelfPermission(this, it)  == PackageManager.PERMISSION_GRANTED
//}

fun Context.isPermissionsGranted() = REQUIRED_PERMISSIONS_Gallery.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

val REQUIRED_PERMISSIONS_Gallery = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

fun remoteKeyforDocumantViewer(): String {
    return if (BuildConfig.DEBUG) {
        "All_Document_Reader_debug"
    } else {
        "All_Document_Reader"
    }
}

fun Context.getStoragePermission(
    onPermissionGranted: (() -> Unit),
    onPermissionDenied: (() -> Unit)? = null
) {

    val permissions =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    Permissions.check(
        this,
        permissions,
        null,
        null,
        object : PermissionHandler() {
            override fun onGranted() {
                onPermissionGranted.invoke()
            }

            override fun onDenied(
                context: Context?,
                deniedPermissions: java.util.ArrayList<String>?
            ) {
                super.onDenied(context, deniedPermissions)
                showToast("Storage permission denied")
                onPermissionDenied?.invoke()
            }
        })

}

fun Context.shareApp() {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            resources.getString(R.string.app_name)
        )
        var shareMessage =
            "\nHi! I Just checked this app in play store, You must try it out:\n\n"
        shareMessage =
            """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(
            Intent.createChooser(
                shareIntent,
                resources.getString(R.string.share_title)
            )
        )
    } catch (e: Exception) {
    }
}

fun Context.launchAnotherApplication(packageName: String) {
    val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        startActivity(launchIntent) //null pointer check in case package name was not found
    } else {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}

fun Context.isAppInstalled(packageName: String): Boolean {
    val pm = packageManager
    try {
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        return true
    } catch (ignored: PackageManager.NameNotFoundException) { }
    return false
}

fun String.isValidFileName(): Boolean {
    return this.toLowerCase(Locale.ROOT).contains(Constants.PDF) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.PPT) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.PPTX) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.docExtension) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.docxExtension) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.excelExtension) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.excelWorkbookExtension) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.excelWorkbookExtension) ||
            this.toLowerCase(Locale.ROOT).contains(Constants.PDF)
}

fun String.getFileNameExtension(): String {
    return substring(lastIndexOf("."))
}

fun String.changeExtension(newExtension: String): String {
    return if (this.contains(".")) {
        val i = lastIndexOf('.')
        val name = substring(0, i)
        name + newExtension
    } else {
        this + newExtension
    }
}

fun ImageView.loadImageThroughBitmap(bitmap: Bitmap? = null) {
    val circularProgressDrawable = CircularProgressDrawable(this.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()
    if (bitmap == null)
        Glide.with(this).load("").placeholder(circularProgressDrawable).into(this)
    else {
        try {
            Glide.with(this).load(bitmap).placeholder(circularProgressDrawable).into(this)
        } catch (e: Exception) {
        }
    }
}

fun Activity.hideStatusBar() {
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    if (Build.VERSION.SDK_INT >= 22) {
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT
    }
}
fun Activity.setWindowFlag(bits: Int, on: Boolean) {
    val win = window
    val winParams = win.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.attributes = winParams
}

fun Context.isAlreadyPurchased(): Boolean {
    val tinyDB = SharedPref(this)
    return tinyDB.getBoolean(Constants.isPremiumUserKey)
}

fun Activity.sendEmail() {
    val addresses = arrayOf("itechsolution.feedback@gmail.com")
    val subject = "Feed back " + applicationContext.getString(R.string.app_name) + ""
    val body =
        "Tell us which issues you are facing using " + applicationContext.getString(R.string.app_name) + " App?"
    try {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        emailIntent.type = "plain/text"
        emailIntent.setClassName(
            "com.google.android.gm",
            "com.google.android.gm.ComposeActivityGmail"
        )
//        emailIntent.setPackage("com.google.android.gm");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(emailIntent)
    } catch (e: Exception) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:" + addresses[0])
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.rateUs() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }

//    val uri = Uri.parse("market://details?id=$packageName")
//    val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
//    try {
//        startActivity(myAppLinkToMarket)
//    } catch (e: ActivityNotFoundException) {
//        Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show()
//    }
}


fun addDelay(time: Long, onComplete: (() -> Unit)) {
    Handler(Looper.getMainLooper()).postDelayed({
        onComplete.invoke()
    }, time)
}

fun Activity.addButtonDelay(value: Long) {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )

    val buttonTimer = Timer()
    buttonTimer.schedule(object : TimerTask() {
        override fun run() {
            runOnUiThread {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }, value)
}

fun Context.bitmapToFile(bitmap: Bitmap): File? {
    var file: File? = null
    return try {
        file = File(getStoragePathPdf() + System.currentTimeMillis())
        file.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapdata = bos.toByteArray()
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        file
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        file
    }
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Context.printPDF(dataMode: DataModel) {
    val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
    val uri = Uri.fromFile(File(dataMode.path))
    runCatching {
        PdfDocumentAdapter(this, uri)
    }.onSuccess { printAdapter ->
        printManager.print(
            "${getString(R.string.app_name)} Document",
            printAdapter,
            PrintAttributes.Builder().build()
        )
    }.onFailure { e -> e.printStackTrace() }
}

fun Context.sizeFormatter(size: Long) = android.text.format.Formatter.formatShortFileSize(this, size)

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.openActivityWithClearTask(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun <T> Context.openActivityAndClearApp(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun getMonthInEng(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "Jan"
    }
}

fun Context.shareDocument(path: String) {
    try {
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                applicationContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        showToast("Error")
    }
}

fun Context.getDrawableResource(restId: Int): Drawable? {
    return ResourcesCompat.getDrawable(resources, restId, theme)
}

fun Context.getColorResource(restId: Int): Int {
    return ResourcesCompat.getColor(resources, restId, theme)
}

fun startScopeFunction(onStart: (() -> Unit), onComplete: (() -> Unit)) {
    GlobalScope.launch(Dispatchers.Main) {
        onStart.invoke()
    }.invokeOnCompletion {
        GlobalScope.launch(Dispatchers.Main) {
            onComplete.invoke()
        }
    }
}

fun startMainScopeFunction(onStart: (() -> Unit), onComplete: (() -> Unit)? = null) {
    GlobalScope.launch(Dispatchers.Main) {
        onStart.invoke()
    }.invokeOnCompletion {
        onComplete?.invoke()
    }
}

fun Bitmap.resizeImage(): Bitmap {

    val width = width
    val height = height

    val scaleWidth = width / 10
    val scaleHeight = height / 10

    if (byteCount <= 100000000)
        return this

    return Bitmap.createScaledBitmap(this, scaleWidth, scaleHeight, false)
}

fun Activity.showKeyboardOnView(view: View) {
    view.post {
        view.requestFocus()
        val imgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.slideUp() {
    val bottomUp: Animation = AnimationUtils.loadAnimation(
        context,
        R.anim.bottom_up
    )
    startAnimation(bottomUp)
    visibility = View.VISIBLE
}

fun View.slideDown() {
    // Hide the Panel
    val bottomDown: Animation = AnimationUtils.loadAnimation(
        context,
        R.anim.bottom_down
    )
    startAnimation(bottomDown)
    visibility = View.INVISIBLE
}

fun Context.getImageUri(inImage: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
    return Uri.parse(path)
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

var ROTATION_FROM_ANGEL = 0f
var ROTATION_TO_ANGEL = 0f

fun View.rotateRight(): Float {
    var rotate: RotateAnimation? = null
    if (ROTATION_FROM_ANGEL == 0f && ROTATION_TO_ANGEL == 90f) {
        rotate = RotateAnimation(
            90f,
            180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 90f
        ROTATION_TO_ANGEL = 180f
    } else if (ROTATION_FROM_ANGEL == 90f && ROTATION_TO_ANGEL == 180f) {
        rotate = RotateAnimation(
            180f,
            270f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 180f
        ROTATION_TO_ANGEL = 270f
    } else if (ROTATION_FROM_ANGEL == 180f && ROTATION_TO_ANGEL == 270f) {
        rotate = RotateAnimation(
            270f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 270f
        ROTATION_TO_ANGEL = 360f
    } else if (ROTATION_FROM_ANGEL == 270f && ROTATION_TO_ANGEL == 360f) {
        rotate = RotateAnimation(
            0f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 0f
        ROTATION_TO_ANGEL = 90f
    } else if (ROTATION_FROM_ANGEL == 0f && ROTATION_TO_ANGEL == -90f) {
        rotate = RotateAnimation(
            -90f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -90f
        ROTATION_TO_ANGEL = 0f
    } else if (ROTATION_FROM_ANGEL == -90f && ROTATION_TO_ANGEL == -180f) {
        rotate = RotateAnimation(
            -180f,
            -90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -180f
        ROTATION_TO_ANGEL = -90f
    } else if (ROTATION_FROM_ANGEL == -180f && ROTATION_TO_ANGEL == -90f) {
        rotate = RotateAnimation(
            -90f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -90f
        ROTATION_TO_ANGEL = 0f
    } else if (ROTATION_FROM_ANGEL == -180f && ROTATION_TO_ANGEL == -270f) {
        rotate = RotateAnimation(
            -270f,
            -180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -270f
        ROTATION_TO_ANGEL = -180f
    } else if (ROTATION_FROM_ANGEL == 180f && ROTATION_TO_ANGEL == 90f) {
        rotate = RotateAnimation(
            90f,
            180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 90f
        ROTATION_TO_ANGEL = 180f
    } else if (ROTATION_FROM_ANGEL == -270f && ROTATION_TO_ANGEL == -180f) {
        rotate = RotateAnimation(
            -180f,
            -90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -180f
        ROTATION_TO_ANGEL = -90f
    } else if (ROTATION_FROM_ANGEL == -270f && ROTATION_TO_ANGEL == -360f) {
        rotate = RotateAnimation(
            0f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 0f
        ROTATION_TO_ANGEL = 90f
    } else {
        rotate = RotateAnimation(
            0f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 0f
        ROTATION_TO_ANGEL = 90f
    }

    Timber.d("$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")
    Log.d(TAG, "RIGHT___$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")
    // snack("$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")
    rotate.apply {
        fillAfter = true
        repeatCount = 0
        duration = 500
        interpolator = LinearInterpolator()
    }
    startAnimation(rotate)

    return ROTATION_TO_ANGEL
}

fun View.rotateLeft(): Float {
    var rotate: RotateAnimation? = null
    if (ROTATION_FROM_ANGEL == 0f && ROTATION_TO_ANGEL == 90f) {
        rotate = RotateAnimation(
            90f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 90f
        ROTATION_TO_ANGEL = 0f
    } else if (ROTATION_FROM_ANGEL == 90f && ROTATION_TO_ANGEL == 180f) {
        rotate = RotateAnimation(
            180f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 180f
        ROTATION_TO_ANGEL = 90f
    } else if (ROTATION_FROM_ANGEL == 180f && ROTATION_TO_ANGEL == 270f) {
        rotate = RotateAnimation(
            270f,
            180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 270f
        ROTATION_TO_ANGEL = 180f
    } else if (ROTATION_FROM_ANGEL == 270f && ROTATION_TO_ANGEL == 180f) {
        rotate = RotateAnimation(
            180f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 180f
        ROTATION_TO_ANGEL = 90f
    } else if (ROTATION_FROM_ANGEL == 180f && ROTATION_TO_ANGEL == 90f) {
        rotate = RotateAnimation(
            90f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 90f
        ROTATION_TO_ANGEL = 0f
    } else if (ROTATION_FROM_ANGEL == 270f && ROTATION_TO_ANGEL == 360f) {
        rotate = RotateAnimation(
            360f,
            270f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 360f
        ROTATION_TO_ANGEL = 270f
    } else if (ROTATION_FROM_ANGEL == 360f && ROTATION_TO_ANGEL == 270f) {
        rotate = RotateAnimation(
            270f,
            180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 270f
        ROTATION_TO_ANGEL = 180f
    } else if (ROTATION_FROM_ANGEL == 0f && ROTATION_TO_ANGEL == -90f) {
        rotate = RotateAnimation(
            -90f,
            -180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -90f
        ROTATION_TO_ANGEL = -180f
    } else if (ROTATION_FROM_ANGEL == -90f && ROTATION_TO_ANGEL == -180f) {
        rotate = RotateAnimation(
            -180f,
            -270f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -180f
        ROTATION_TO_ANGEL = -270f
    } else if (ROTATION_FROM_ANGEL == -180f && ROTATION_TO_ANGEL == -270f) {
        rotate = RotateAnimation(
            -270f,
            -360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -270f
        ROTATION_TO_ANGEL = -360f
    } else if (ROTATION_FROM_ANGEL == -270f && ROTATION_TO_ANGEL == -360f) {
        rotate = RotateAnimation(
            0f,
            -90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 0f
        ROTATION_TO_ANGEL = -90f
    } else if (ROTATION_FROM_ANGEL == -270f && ROTATION_TO_ANGEL == -180f) {
        rotate = RotateAnimation(
            -180f,
            -90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -180f
        ROTATION_TO_ANGEL = -90f
    } else if (ROTATION_FROM_ANGEL == -180f && ROTATION_TO_ANGEL == -90f) {
        rotate = RotateAnimation(
            -90f,
            -180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = -90f
        ROTATION_TO_ANGEL = -180f
    } else {
        rotate = RotateAnimation(
            0f,
            -90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        ROTATION_FROM_ANGEL = 0f
        ROTATION_TO_ANGEL = -90f
    }

    Timber.d("$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")
    Log.d(TAG, "LEFT___$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")
    //  snack("$ROTATION_FROM_ANGEL , $ROTATION_TO_ANGEL")

    rotate.apply {
        fillAfter = true
        repeatCount = 0
        duration = 500
        interpolator = LinearInterpolator()
    }
    startAnimation(rotate)

    return ROTATION_TO_ANGEL
}

private const val TAG = "ExtensionFunctions"

fun ImageView.getPdfBitmap(file: File?, pageNo: Int): Bitmap? {
//    var bitmap: Bitmap? = null
//    try {
//        val renderer = PdfRenderer(
//            ParcelFileDescriptor.open(
//                file,
//                ParcelFileDescriptor.MODE_READ_ONLY
//            )
//        )
//        val page = renderer.openPage(pageNo)
//        val width: Int = resources.displayMetrics.densityDpi / 72 * page.width
//        val height: Int = resources.displayMetrics.densityDpi / 72 * page.height
//        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//        page.close()
//        renderer.close()
//    } catch (ex: java.lang.Exception) {
//        ex.printStackTrace()
//    }
//
//    return bitmap

    var bitmap: Bitmap? = null
    try {
        val pdfiumCore = PdfiumCore(this.context)
        val pdfDocument: com.shockwave.pdfium.PdfDocument = pdfiumCore.newDocument(openFile(file))
        pdfiumCore.openPage(pdfDocument, pageNo)
        val width: Int = pdfiumCore.getPageWidthPoint(pdfDocument, pageNo)
        val height: Int = pdfiumCore.getPageHeightPoint(pdfDocument, pageNo)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNo, 0, 0, width, height)
        pdfiumCore.closeDocument(pdfDocument)
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return bitmap
}

fun ImageView.getPdfBitmapAdapter(file: File?, pageNo: Int): Bitmap? {
//    var bitmap: Bitmap? = null
//    try {
//        val renderer = PdfRenderer(
//            ParcelFileDescriptor.open(
//                file,
//                ParcelFileDescriptor.MODE_READ_ONLY
//            )
//        )
//        val page = renderer.openPage(pageNo)
//        val width: Int = resources.displayMetrics.densityDpi / 72 * page.width
//        val height: Int = resources.displayMetrics.densityDpi / 72 * page.height
//        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//        page.close()
//        renderer.close()
//    } catch (ex: java.lang.Exception) {
//        ex.printStackTrace()
//    }
//
//    return bitmap

    var bitmap: Bitmap? = null
    try {
        val pdfiumCore = PdfiumCore(this.context)
        val pdfDocument: com.shockwave.pdfium.PdfDocument = pdfiumCore.newDocument(openFile(file))
        pdfiumCore.openPage(pdfDocument, pageNo)
        val width: Int = pdfiumCore.getPageWidthPoint(pdfDocument, pageNo)
        val height: Int = pdfiumCore.getPageHeightPoint(pdfDocument, pageNo)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNo, 0, 0, width, height)
        pdfiumCore.closeDocument(pdfDocument)
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return bitmap

}

fun ImageView.getPdfBitmapAdapter2(file: File, pageNo: Int, onResult: ((Bitmap) -> Unit)) {
    GlobalScope.launch(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            val pdfiumCore = PdfiumCore(this@getPdfBitmapAdapter2.context)
            val pdfDocument: com.shockwave.pdfium.PdfDocument =
                pdfiumCore.newDocument(openFile(file))
            pdfiumCore.openPage(pdfDocument, pageNo)
            val width: Int = pdfiumCore.getPageWidthPoint(pdfDocument, pageNo)
            val height: Int = pdfiumCore.getPageHeightPoint(pdfDocument, pageNo)
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNo, 0, 0, width, height)
            pdfiumCore.closeDocument(pdfDocument)
            withContext(Dispatchers.Main) {
                onResult.invoke(bitmap)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                bitmap?.let { onResult.invoke(it) }
            }
        }
    }
}


fun Context.pdfToBitmap(pdfFile: File): ArrayList<Bitmap> {
    val bitmaps: ArrayList<Bitmap> = ArrayList()
    try {
        val renderer = PdfRenderer(
            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        var bitmap: Bitmap
        val pageCount = renderer.pageCount
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val width: Int = resources.displayMetrics.densityDpi / 72 * page.width
            val height: Int = resources.displayMetrics.densityDpi / 72 * page.height
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            page.close()
        }
        renderer.close()
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
    return bitmaps
}

private fun openFile(file: File?): ParcelFileDescriptor? {
    return try {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }
}

fun ImageView.loadImageWithGlide(url: String? = null, bitmap: Bitmap? = null) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    if (url != null) {
        Glide.with(this)
            .load(url)
            .placeholder(circularProgressDrawable)
            .into(this)
    } else if (bitmap != null) {
        Glide.with(this)
            .load(bitmap)
            .placeholder(circularProgressDrawable)
            .into(this)
    }
}

fun ImageView.getBitmapFromImageView(): Bitmap {
    invalidate()
    val drawable: BitmapDrawable = drawable as BitmapDrawable
    return drawable.bitmap
}

fun loadBitmap(url: Uri?): Bitmap? {
    var bm: Bitmap? = null
    var `is`: InputStream? = null
    var bis: BufferedInputStream? = null
    try {
        val conn: URLConnection = URL(url.toString()).openConnection()
        conn.connect()
        `is` = conn.getInputStream()
        bis = BufferedInputStream(`is`, 8192)
        bm = BitmapFactory.decodeStream(bis)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (bis != null) {
            try {
                bis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (`is` != null) {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return bm
}

fun ImageView.loadPageImageFromPdf(path: String, pageNo: Int): Bitmap? {
    val file = File(path)
    val bitmap = getPdfBitmap(file, pageNo)
    Glide.with(this)
        .load(bitmap)
        .into(this)

    return bitmap
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Activity.copyToClipboard(text: String) {
    val clipboardManager =
        getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.text = text
    Toast.makeText(applicationContext, "Copied", Toast.LENGTH_SHORT).show()
}

fun Context.share(text: String, subject: String = ""): Boolean {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, null))
        return true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        return false
    }
}

//fun isStoragePermissionGranted(): Boolean {
//    return if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
//        ContextCompat.checkSelfPermission(
//            getContext(),
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    } else {
//        true
//    }
//}

//fun getRuntimePermissions() {
//    if (Build.VERSION.SDK_INT < 29) {
//        PermissionsUtils.getInstance().requestRuntimePermissions(
//            this,
//            WRITE_PERMISSIONS,
//            REQUEST_CODE_FOR_WRITE_PERMISSION
//        )
//    }
//}

fun Activity.changeStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, color)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.changeStatusBarWindow(color: Int) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    window.statusBarColor = ContextCompat.getColor(this, color)
}