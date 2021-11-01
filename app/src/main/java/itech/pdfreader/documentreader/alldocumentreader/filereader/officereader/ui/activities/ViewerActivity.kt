package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.barteksc.pdfviewer.listener.*
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivityDocumentViewBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.AdModelClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.ScrollerAdsAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.GotoPageDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.PasswordProtectedDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.ViewerToolsBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable

class ViewerActivity : BaseActivity(), OnLoadCompleteListener, OnErrorListener,
    OnPageScrollListener, OnTapListener, ViewerToolsBottomSheet.ItemClickListener,
    OnRenderListener {

    private var type: String = ""
    var size: String? = null
    var path: String? = null
    private var pass: String = ""
    private var noOfPages: Int = 0
    lateinit var binding: ActivityDocumentViewBinding
    private var dataModel: DataModel? = null
    private var isFullView = false
    private var timerRunnable: Runnable? = null
    private var timerHandler: Handler? = null
    private var viewDelay: Long = 1000
    private val TAG = "DocumentViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentViewBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initView()
        setListeners()
    }

    private fun getIntentValues() {
        val intent = intent
        if (intent != null) {
            if (Intent.ACTION_VIEW == intent.action) {
                intent.data?.let { finalUri ->
                    try {
                        val file = from(finalUri)
                        dataModel = getDataModelFromFile(file)
                    } catch (e: Exception) {
                        showToast("Could not get file")
                        finish()
                    }
                } ?: run {
                    showToast("Could not get file")
                    finish()
                }
            } else {
                dataModel = intent.getSerializableExtra(Constants.DOC_MODEL) as DataModel?
            }
        } else {
            onBackPressed()
        }
    }

    private fun initView() {
        getIntentValues()
        refreshAd()
        binding.apply {
            header.backBtn.setOnClickListener {
                onBackPressed()
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            var file: File? = null
            async {
                file = File(dataModel?.path ?: "")
            }.invokeOnCompletion {
                file?.let {
                    if (!it.exists()) {
                        GlobalScope.launch(Dispatchers.Default) {
                            dataModel?.id?.toLong()
                                ?.let { it1 -> dataViewModel.deleteDocModel(it1) }
                        }.invokeOnCompletion {
                            GlobalScope.launch(Dispatchers.Main) {
                                showToast("File not exist")
                                finish()
                            }
                        }
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            binding.docModel = this@ViewerActivity.dataModel
                            pass = ""
                            dataModel?.let { docModel ->
                                if (!docModel.isLock) {
                                    binding.header.headerTxt.visibility = View.VISIBLE
                                    binding.header.headerTxt.text = docModel.name
                                    binding.header.headerTxt.isSelected = true
                                    showPDF(docModel)
                                    GlobalScope.launch(Dispatchers.Default) {
                                        if (!docModel.isRecent) {
                                            Companions.isRecentAdded = true
//                                            mainViewModel.addRecent(docModel.id, true)
                                            if (dataViewModel.findDataModel(docModel.path)
                                                    .isEmpty()
                                            ) {
                                                docModel.isRecent = true
                                                dataViewModel.addDataModel(docModel)
                                            } else {
                                                dataViewModel.addRecent(docModel.path, true)
                                            }
                                        }
                                    }
                                } else {
                                    showPasswordDialog(docModel)
                                }
                            }
                            Handler(Looper.getMainLooper()).postDelayed({
//                                binding.pdfNameL.visibility = View.GONE
                                binding.pageNoTv.visibility = View.GONE
                            }, viewDelay)
                        }
                    }
                }
            }
        }
    }

    var index = 0
    var sliderDelay = 4000L
    private fun setAutoScrollRecyclerView() {
        timerHandlerAd = Handler(Looper.myLooper()!!)
        object : Runnable {
            override fun run() {
                timerHandlerAd?.removeCallbacks(this)
                if (index <= adsList.size - 1) {
                    binding.adLayout.smoothScrollToPosition(index)
                    index++
                    timerHandlerAd?.postDelayed(this, sliderDelay)
                } else {
                    index = 0
                    binding.adLayout.smoothScrollToPosition(index)
                    index++
                    timerHandlerAd?.postDelayed(this, sliderDelay)
                }
            }
        }.also { timerRunnableAd = it }
        timerHandlerAd?.postDelayed(timerRunnableAd as Runnable, sliderDelay)
    }

    override fun onResume() {
        super.onResume()
        val cacheBitmap = MyCache.getInstance().retrieveBitmapFromCache(Constants.BITMAP_STICKER)
        if (cacheBitmap != null)
            MyCache.getInstance().retrieveBitmapFromCache(Constants.BITMAP_STICKER).recycle()
        setAutoScrollRecyclerView()
    }

    override fun onPause() {
        super.onPause()
        timerRunnableAd?.let { timerHandlerAd?.removeCallbacks(it) }
    }

    override fun onStop() {
        super.onStop()
        timerRunnableAd?.let { timerHandlerAd?.removeCallbacks(it) }
    }

    private var timerRunnableAd: Runnable? = null
    private var timerHandlerAd: Handler? = null
    private var adsList = mutableListOf<AdModelClass>()
    private fun refreshAd() {
        /**show native ad*/
        if (!utilsViewModel.isPremiumUser() && isInternetConnected()) {
            adsList.add(AdModelClass("", "", "", "", "", Constants.NATIVE_AD))
            utilsViewModel.remoteConfigModel?.let { it ->
                if (!it.crossPromotionAppsData?.appsLinks.isNullOrEmpty()) {
                    it.crossPromotionAppsData?.appsLinks?.forEach {
                        adsList.add(
                            AdModelClass(
                                it.appShortDesc.toString(),
                                it.appShortDesc.toString(),
                                "INSTALL",
                                it.appCoverLink.toString(),
                                it.appIconLink.toString(),
                                it.appLink.toString()
                            )
                        )
                    }
                } else {
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
        }
        val adapterScroller = ScrollerAdsAdapter(true)
        adapterScroller.differ.submitList(adsList)
        adapterScroller.setOnItemClickListener {
            if (it.adType != Constants.NATIVE_AD)
                openUrl(it.adType)
        }
        binding.adLayout.layoutManager =
            StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL)
        binding.adLayout.adapter = adapterScroller
        binding.adLayout.adapter = adapterScroller
        /************************/
    }

    private fun showPasswordDialog(dataModel: DataModel) {
        val dialog = PasswordProtectedDialog(context = this, onResult = {
            pass = it
            showPDF(dataModel)
            GlobalScope.launch(Dispatchers.Default) {
                dataViewModel.addRecent(
                    dataModel.path,
                    true
                )
            }
        }, onCancel = {
            onBackPressed()
        })
        dialog.show()
    }

    override fun onBackPressed() {
        if (isFullView)
            closeFullView()
        else {
            if (intent != null) {
                if (Intent.ACTION_VIEW == intent.action) {
                    finishAffinity()
                    openActivity(DashboardActivity::class.java)
                } else {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            header.menuBtn.setOnClickListener {
                addButtonDelay(1000)
                supportFragmentManager.let {
                    ViewerToolsBottomSheet.newInstance(Bundle().apply {
                        putSerializable(Constants.DOC_MODEL, docModel)
                    }).apply {
                        show(it, tag)
                        mListener = this@ViewerActivity
                    }
                }
            }

            header.shareBtn.setOnClickListener {
                shareDocument(docModel?.path.toString())
            }
            addSignatureBtn.setOnClickListener {
                type = "PdfPagesGridActivity"
                showInterAdThenStartNextProcess()
            }
        }
    }

    private fun showPDF(pdfFile: DataModel, defaultPage: Long = 0) {
        if (pdfFile.path.isBlank())
            showToast("Fail to Load")
        else {
            val file = File(pdfFile.path)
            binding.pdfView.useBestQuality(true)
            binding.pdfView.fromUri(Uri.fromFile(file))
                .defaultPage(defaultPage.toInt())
                .password(pass)
                .enableAnnotationRendering(true)
                .onPageScroll(this)
                .onLoad(this)
                .onError(this)
                .scrollHandle(DefaultScrollHandle(this))
                .autoSpacing(true) // add dynamic spacing to fit each page on its own on the screen
                .fitEachPage(true) // fit each page to the view, else smaller pages are scaled relative to largest page.
                .onTap(this)
                .onRender(this)
                .enableAntialiasing(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .spacing(23)
                .swipeHorizontal(sharedPref.getBoolean(Constants.IS_VIEWER_HORIZONTAL_VIEW))
                .nightMode(sharedPref.getBoolean(Constants.IS_VIEWER_NIGHT_MODE))
                .onPageError { page, _ ->
                    showToast("Error at page: $page")
                }
                .load()
        }
    }

    override fun onError(t: Throwable?) {
        if (pass.isNotEmpty())
            showToast("Wrong Password")
        dataModel?.let { showPasswordDialog(it) }
    }

    override fun loadComplete(nbPages: Int) {
        noOfPages = nbPages
    }

    override fun onPageScrolled(page: Int, positionOffset: Float) {
        val str = "Page ${page + 1}/${noOfPages}"
        binding.pageNoTv.text = str
        visibleViewsWithDelay()
    }

    override fun onTap(e: MotionEvent?): Boolean {
        visibleViewsWithDelay()
        if (isFullView)
            closeFullView()
        else
            showFullView()
        return true
    }

    private fun showFullView() {
        binding.header.root.visibility = View.GONE
        isFullView = true
    }

    private fun closeFullView() {
        binding.header.root.visibility = View.VISIBLE
        isFullView = false
    }

    private fun visibleViewsWithDelay() {
//        binding.pdfNameL.visibility = View.VISIBLE
        binding.pageNoTv.visibility = View.VISIBLE
        if (timerHandler != null) {
            timerRunnable?.let { timerHandler?.removeCallbacks(it) }
            timerRunnable?.let { timerHandler?.postDelayed(it, viewDelay) }
        } else {
            timerHandler = Handler(Looper.myLooper()!!)
            timerRunnable = object : Runnable {
                override fun run() {
                    timerHandler?.removeCallbacks(this)
//                    binding.pdfNameL.visibility = View.GONE
                    binding.pageNoTv.visibility = View.GONE
                    timerHandler?.postDelayed(this, viewDelay)
                }
            }
        }
    }

    override fun onItemClick(item: String) {
        when (item) {
            ViewerToolsBottomSheet.IS_NIGHT_MODE -> {
                if (sharedPref.getBoolean(Constants.IS_VIEWER_NIGHT_MODE)) {
                    binding.pdfView.setNightMode(true)
                } else {
                    binding.pdfView.setNightMode(false)
                }
                binding.pdfView.loadPages()
            }
            ViewerToolsBottomSheet.IS_HORIZONTAL_VIEW -> {
                if (sharedPref.getBoolean(Constants.IS_VIEWER_HORIZONTAL_VIEW)) {
                    sharedPref.putBoolean(Constants.IS_VIEWER_HORIZONTAL_VIEW, false)
                    dataModel?.let { showPDF(it) }
                } else {
                    sharedPref.putBoolean(Constants.IS_VIEWER_HORIZONTAL_VIEW, true)
                    dataModel?.let { showPDF(it) }
                }
            }
            ViewerToolsBottomSheet.PAGE_BY_PAGE -> {
                type = "EditorActivity"
                showInterAdThenStartNextProcess()
            }
            ViewerToolsBottomSheet.SCAN_DOC -> {
                type = "PdfEditorPagesGridActivity_SCAN_DOC"
                showInterAdThenStartNextProcess()
            }
            ViewerToolsBottomSheet.GOTO -> {
                val dialog = GotoPageDialog(this) {
                    try {
                        if (it.toInt() in 1..noOfPages) {
                            val str = "Page ${binding.pdfView.currentPage.toLong()}/${noOfPages}"
                            binding.pageNoTv.text = str
                            dataModel?.let { it1 -> showPDF(it1, it.toLong() - 1) }
                        } else
                            binding.root.snack("Page not exist")
                    } catch (e: Exception) {
                        binding.root.snack("Page not exist")
                    }
                }
                dialog.show()
            }
        }
    }

    private fun showInterAdThenStartNextProcess() {
        if (Companions.viewerScreenCounter == Companions.globalInterAdCounter) {
            InterstitialAdsUtils.getInstance().showInterstitialAdNew(this) {
                startNextScreen(type)
            }
            Companions.viewerScreenCounter = 0
        } else {
            Companions.viewerScreenCounter++
            startNextScreen(type)
        }
    }

    private fun startNextScreen(type: String) {
        when (type) {
            "EditorActivity" -> {
                openActivity(PageByPageViewActivity::class.java) {
                    putString(Constants.DOC_MODEL, dataModel?.path)
                    putInt(Constants.NO_OF_PAGES, noOfPages)
                    putInt(Constants.PAGE_NO, binding.pdfView.currentPage)
                    putBoolean(Constants.IS_FROM_VIEWER, true)
                }
            }
        }
    }

    override fun onInitiallyRendered(nbPages: Int) {}
}