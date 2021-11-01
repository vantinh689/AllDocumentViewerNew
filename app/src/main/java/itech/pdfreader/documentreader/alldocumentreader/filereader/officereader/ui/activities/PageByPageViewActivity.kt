package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.itextpdf.text.pdf.PdfReader
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivityPageByPageViewBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.FilePageModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.PdfPagesSliderAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class PageByPageViewActivity : BaseActivity() {

    private var noOfPages: Int = 0
    lateinit var binding: ActivityPageByPageViewBinding
    private var adapter: PdfPagesSliderAdapter? = null
    private var pageModelList = mutableListOf<FilePageModel>()

    private var timerRunnable: Runnable? = null
    private var timerHandler: Handler? = null
    private var viewDelay: Long = 1000

    private var path: String = ""
    private var pageNo: Int? = null
    private var isFromViewer: Boolean = false

    var pdfReader: PdfReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageByPageViewBinding.inflate(layoutInflater)
        initViews()
        setListeners()
        setContentView(binding.root)
        refreshAdOnView()
    }

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        if (!utilsViewModel.isPremiumUser() && isInternetConnected()) {
            binding.adLayout.visibility = View.VISIBLE
            setNativeAd(
                utilsViewModel.isPremiumUser(),
                binding.adLayout,
                R.layout.view_screen_native_layout,
                TAG,
                adMobNativeId = getString(R.string.pageEditorScreenNativeId), onFailed = {
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.adLayout.visibility = View.GONE
                    binding.shimmerViewContainer.stopShimmer()
                }
            ) {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
    }

    private fun setListeners() {
        binding.apply {
            header.backBtn.setOnClickListener {
                onBackPressed()
            }
        }
        adapter?.setOnItemClickListener { _, _ ->
            visibleViewsWithDelay()
        }
    }

    private fun initViews() {
        getIntentValues()

        try {
            pdfReader = PdfReader(path)
        } catch (e: Exception) {
            showToast("Failed to open PDF")
            finish()
        }

        val fileName = File(path).name
        if (isFromViewer) {
            binding.bottomView.visibility = View.GONE
        }
        binding.apply {
            header.headerTxt.visibility = View.VISIBLE
            docName = fileName
            header.headerTxt.text = fileName
            pgNo = pageNo.toString()
            totalNoOfPages = noOfPages.toString()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.pdfNameL.visibility = View.GONE
            binding.pageNoTv.visibility = View.GONE
        }, viewDelay)
        adapter = PdfPagesSliderAdapter()
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.pgNo = (position + 1).toString()
                pageNo = position
                Log.d(TAG, "PageNo1:++${pageNo.toString()}")
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                visibleViewsWithDelay()
            }
        })
        val process = GlobalScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            for (i in 0 until noOfPages) {
                path.let { FilePageModel(i + 1, it) }.let {
                    pageModelList.add(it)
                }
            }
            adapter?.differ?.submitList(pageModelList)
//            Log.d(TAG, pageNo.toString())
            binding.viewPager.setCurrentItem((pageNo ?: 0) - 1, false)
        }
        process.invokeOnCompletion {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getIntentValues() {
        val intent = intent
        if (intent != null) {
            if (Intent.ACTION_VIEW == intent.action) {
                intent.data?.let { finalUri ->
                    try {
                        val file = from(finalUri)
                        path = file.path
                        try {
                            val pdfReader = PdfReader(path)
                            noOfPages = pdfReader.numberOfPages
                        } catch (e: Exception) {
                            finish()
                            showToast("Could not get file")
                        }
                        pageNo = 0
                        isFromViewer = false
                    } catch (e: Exception) {
                        finish()
                        showToast("Could not get file")
                    }
                } ?: showToast("Could not get file")
            } else {
                path = intent.getStringExtra(Constants.DOC_MODEL).toString()
                noOfPages = intent.getIntExtra(Constants.NO_OF_PAGES, 0)
                pageNo = intent.getIntExtra(Constants.PAGE_NO, 0)
                isFromViewer = intent.getBooleanExtra(Constants.IS_FROM_VIEWER, false)
            }
        } else {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Companions.newPath.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                pageModelList.clear()
                for (i in 0 until noOfPages) {
                    pageModelList.add(FilePageModel(i + 1, Companions.newPath))
                }
                path = Companions.newPath
                Companions.newPath = ""
                adapter?.differ?.submitList(pageModelList)
                Log.d(TAG, "PageNo2:++${pageNo.toString()}")
            }.invokeOnCompletion {
                binding.viewPager.setCurrentItem((pageNo ?: 0), false)
                adapter?.notifyItemChanged(pageNo ?: 0)

            }
        }
    }

    private val TAG = "EditorActivity"

    private fun visibleViewsWithDelay() {
        binding.pdfNameL.visibility = View.VISIBLE
        binding.pageNoTv.visibility = View.VISIBLE
        if (timerHandler != null) {
            timerRunnable?.let { timerHandler?.removeCallbacks(it) }
            timerRunnable?.let { timerHandler?.postDelayed(it, viewDelay) }
        } else {
            timerHandler = Handler(Looper.myLooper()!!)
            timerRunnable = object : Runnable {
                override fun run() {
                    timerHandler?.removeCallbacks(this)
                    binding.pdfNameL.visibility = View.GONE
                    binding.pageNoTv.visibility = View.GONE
                    timerHandler?.postDelayed(this, viewDelay)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (intent != null) {
            if (Intent.ACTION_VIEW == intent.action) {
                finishAffinity()
                openActivity(DashboardActivity::class.java)
            } else
                super.onBackPressed()
        } else
            super.onBackPressed()
    }
}