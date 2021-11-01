package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.skydoves.powermenu.PowerMenuItem
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentHomeBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.AdModelClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.ToolClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.remote.AppsLink
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DashboardActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DocumentsActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.ScrollerAdsAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.FileTypeAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.ViewPagerAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.RateDialogNew
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.BaseFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.tabs.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.tabs.BookmarkFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showSortingDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.globalInterAdCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.homeScreenCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isAllFilesGridChange
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isCallHomeFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isMyFilesGridChange
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isRecentGridChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment() {
    //    private var interstitialAdTabs: Any? = null

    companion object{
        val crossPromotionRemoteList = mutableListOf<AppsLink>()
    }

    private var topAdapterButtonClickText: String = ""
    private var timerRunnable: Runnable? = null
    private lateinit var timerHandler: Handler
    lateinit var binding: FragmentHomeBinding

    lateinit var adapterScroller: ScrollerAdsAdapter
    private lateinit var toolsAdapter: FileTypeAdapter

    private var adsList = mutableListOf<AdModelClass>()
    private var toolsList = mutableListOf<ToolClass>()

    var sliderDelay = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!utilsViewModel.isPremiumUser() && requireContext().isInternetConnected()) {
            adsList.add(AdModelClass("", "", "", "", "", Constants.NATIVE_AD))
            utilsViewModel.remoteConfigModel?.let { it ->
                if (!it.crossPromotionAppsData?.appsLinks.isNullOrEmpty()) {
                    crossPromotionRemoteList.clear()
                    it.crossPromotionAppsData?.appsLinks?.let { it1 ->
                        crossPromotionRemoteList.addAll(it1)
                    }
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

        toolsList.add(
            ToolClass(
                R.drawable.all_doc_ic,
                getString(R.string.all_files),
                R.color.colorTool1,
                Constants.HOME_TOOLS
            )
        )
        toolsList.add(
            ToolClass(
                R.drawable.pdf_ic,
                getString(R.string.pdf),
                R.color.colorTool2,
                Constants.HOME_TOOLS
            )
        )
        toolsList.add(
            ToolClass(
                R.drawable.word_ic,
                getString(R.string.woed_str),
                R.color.colorTool3,
                Constants.HOME_TOOLS
            )
        )
        toolsList.add(
            ToolClass(
                R.drawable.ppt_ic,
                getString(R.string.ppt_str),
                R.color.colorTool4,
                Constants.HOME_TOOLS
            )
        )
        toolsList.add(
            ToolClass(
                R.drawable.excel_ic,
                getString(R.string.excel_str),
                R.color.colorTool5,
                Constants.HOME_TOOLS
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        initViews()


        setListeners()


        return binding.root
    }

    private val TAG = "HomeFragment"

    private fun initViews() {
        adapterScroller = ScrollerAdsAdapter()
        toolsAdapter = FileTypeAdapter()

        adapterScroller.differ.submitList(adsList)
        toolsAdapter.differ.submitList(toolsList)

        binding.apply {

            setupViewPager(viewPager)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapterScroller
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            toolRv.layoutManager = layoutManager
            toolRv.setHasFixedSize(true)
            toolRv.adapter = toolsAdapter
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all_pdf_files)))
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.recent_files)))
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.starred)))
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewPager.currentItem = tab?.position!!
                }
            })
//            viewPager.offscreenPageLimit = 3
//            binding.viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    tabLayout.selectTab(tabLayout.getTabAt(position))
                    GlobalScope.launch(Dispatchers.Main) {
                        dataViewModel.getMyFiles()
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            if (isCallHomeFragment) {
                //  moveViewPagerToMyFiles()
                isCallHomeFragment = false
            }
        }
    }

    private fun setListeners() {
        binding.gridBtn.setOnClickListener {
            if (!sharedPref.getBoolean(Constants.GRID_VALUE)) {
                sharedPref.putBoolean(Constants.GRID_VALUE, true)
            } else {
                sharedPref.putBoolean(Constants.GRID_VALUE, false)
            }
            isAllFilesGridChange = true
            isRecentGridChange = true
            isMyFilesGridChange = true
            dataViewModel.updateData()
        }
        binding.searchBtn.setOnClickListener {
//            (activity as DashboardActivity).navController.navigate(R.id.action_navigation_home_to_searchFragment)
        }
        binding.sortBtn.setOnClickListener {
            requireActivity().addButtonDelay(500)
            (requireContext() as AppCompatActivity).showSortingDialog(it,
                onResult = { i: Int, _: PowerMenuItem ->
                    Log.d("TAG123", "outer123444")
                    Companions.isAllFilesLoaded = false
                    when (i) {
                        0 -> {
                            Companions.sortType = Constants.DATE
                            GlobalScope.launch(Dispatchers.Main) {
                                dataViewModel.getAllSearchResults("")
                            }
                        }
                        1 -> {
                            Companions.sortType = Constants.NAME
                            GlobalScope.launch(Dispatchers.Main) {
                                dataViewModel.getAllSearchResults("")
                            }
                        }
                        2 -> {
                            Companions.sortType = Constants.SIZE
                            GlobalScope.launch(Dispatchers.Main) {
                                dataViewModel.getAllSearchResults("")
                            }
                        }
                    }
                    dataViewModel.setObserveChanges(true)
                },
                onDismissResult = {})
        }

        toolsAdapter.setOnItemClickListener { toolClass: ToolClass, i: Int ->
            requireActivity().addButtonDelay(1500)
            topAdapterButtonClickText = toolClass.text

            if (homeScreenCounter == globalInterAdCounter) {
                InterstitialAdsUtils.getInstance().showInterstitialAdNew(requireActivity()) {
                    setTopAdapterButtonClick(toolClass.text)
                }
                homeScreenCounter = 0
            } else {
                homeScreenCounter++
                setTopAdapterButtonClick(toolClass.text)
            }
        }
        adapterScroller.setOnItemClickListener {
            if (it.adType != Constants.NATIVE_AD)
                requireActivity().openUrl(it.adType)
        }
    }

    private fun openDrawer() {
        (activity as DashboardActivity).binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setTopAdapterButtonClick(text: String) {
        when (text) {
            getString(R.string.all_files) -> {
                activity?.openActivity(DocumentsActivity::class.java){
                    putString(Constants.TYPE, Constants.AllFiles)
                }
            }
            getString(R.string.pdf) -> {
                activity?.openActivity(DocumentsActivity::class.java){
                    putString(Constants.TYPE, Constants.PDF)
                }
            }
            getString(R.string.woed_str) -> {
                activity?.openActivity(DocumentsActivity::class.java){
                    putString(Constants.TYPE, Constants.docExtension)
                }
            }
            getString(R.string.ppt_str) -> {
                activity?.openActivity(DocumentsActivity::class.java){
                    putString(Constants.TYPE, Constants.PPT)
                }
            }
            getString(R.string.excel_str) -> {

                activity?.openActivity(DocumentsActivity::class.java){
                    putString(Constants.TYPE, Constants.excelExtension)
                }
            }
        }
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    var index = 0

    private fun setAutoScrollRecyclerView() {
        timerHandler = Handler(Looper.myLooper()!!)
        timerRunnable = object : Runnable {
            override fun run() {
                timerHandler.removeCallbacks(this)
                if (index <= adsList.size - 1) {
                    binding.recyclerView.smoothScrollToPosition(index)
                    index++
                    timerHandler.postDelayed(this, sliderDelay)
                } else {
                    index = 0
                    binding.recyclerView.smoothScrollToPosition(index)
                    index++
                    timerHandler.postDelayed(this, sliderDelay)
                }
            }
        }
        timerHandler.postDelayed(timerRunnable as Runnable, sliderDelay)
    }

    override fun onResume() {
        super.onResume()
        setAutoScrollRecyclerView()
    }

    override fun onPause() {
        super.onPause()
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    override fun onStop() {
        super.onStop()
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = ViewPagerAdapter(requireActivity())
        adapter.addFragment(AllDocumentsFragment())
        adapter.addFragment(RecentFilesFragment())
        adapter.addFragment(BookmarkFragment())
        viewPager.adapter = adapter
    }
}