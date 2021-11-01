package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.nativead.NativeAdOptions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.BuildConfig
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.refreshPreLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.ActivityMainBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.inappupdate.InAppUpdate
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.ViewPagerAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.bookmarkFromOtherModule
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.deleteFromOtherModule
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.dialogs.RateDialogNew
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation.DashboardFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation.SearchFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation.SettingsFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.AllowPermissionBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.ExitBottomSheetDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.renameFromOtherModule
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.bottomSheetCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.globalInterAdCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isCallHomeFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.isRecentAdded
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.preLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants.isRatingDoneFirstTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
//    lateinit var navController: NavController
    private val TAG = "MainActivity"
    private lateinit var inAppUpdate: InAppUpdate
    private var prevMenuItem: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inAppUpdate = InAppUpdate(this)
        inAppUpdate.checkUpdate {
            Log.e(TAG, "onCreate: $it")
            if (it == true) {
                currentFocus?.snack("Update available")
            }
        }
        Companions.isAllFilesLoaded = false
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (sharedPref.getBoolean(isRatingDoneFirstTime))
            preLoadBottomSheetNativeAd()
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHostFragment.navController
        binding.apply {
            setContentView(root)
            setupViewPagerBottomSheet(viewPagerMain)
            viewPagerMain.isUserInputEnabled = false
            viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {}
                override fun onPageSelected(position: Int) {
                    if (prevMenuItem != null)
                        prevMenuItem!!.isChecked = false
                    else
                        bottomNavigationView.menu.getItem(0).isChecked = false
                    Log.d("page", "onPageSelected: $position")
                    bottomNavigationView.menu.getItem(position).isChecked = true
                    prevMenuItem = bottomNavigationView.menu.getItem(position)
                }
                override fun onPageScrollStateChanged(state: Int) {}
            })
            bottomNavigationView.setOnItemSelectedListener { item ->
                Companions.isAllFilesLoaded = false
                if (bottomSheetCounter == globalInterAdCounter) {
                    InterstitialAdsUtils.getInstance().showInterstitialAdNew(this@DashboardActivity)
                    bottomSheetCounter = 0
                } else {
                    bottomSheetCounter++
                }
                when (item.itemId) {
                    R.id.navigation_home -> {
                        viewPagerMain.setCurrentItem(0, false)
                    }
                    R.id.searchFragment -> {
                        viewPagerMain.setCurrentItem(1, false)
                    }
                    R.id.settingsFragment -> {
                        viewPagerMain.setCurrentItem(2, false)
                    }
                }
                return@setOnItemSelectedListener false
            }
        }

        renameFromOtherModule(dataViewModel)
        deleteFromOtherModule(dataViewModel)
        bookmarkFromOtherModule(dataViewModel)

        dataViewModel.isHomeScreenObserver.observe(this,{
            progressDialog.show()
            dataViewModel.retrieveFilesFormDevice {
                progressDialog.dismiss()
                dataViewModel.setObserveChanges(true)
            }
        })

        utilsViewModel.onSaveFileHomeFragmentCallback = {
            isCallHomeFragment = true
//            navController.navigate(R.id.navigation_home)
        }
        setDrawerClickListeners()
        if (utilsViewModel.isPremiumUser()) {
            binding.header.subscriptionBtn.visibility = View.GONE
        } else
            binding.header.subscriptionBtn.visibility = View.VISIBLE
        binding.header.notificationBtn.setOnClickListener {
            it.snack("Coming soon")
        }
        binding.header.subscriptionBtn.setOnClickListener {
            binding.root.snack("Coming soon")
            // context?.openActivity(SubscriptionActivity::class.java)
        }
        binding.header.rateUsBtn.setOnClickListener {
            addButtonDelay(1000)
            RateDialogNew(this).createRateUsDialog(false, sharedPref)
        }
        binding.header.drawerBtn.setOnClickListener {
            refreshAdOnView()
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        dataViewModel.allFiles.observe(this@DashboardActivity, { files ->
            if (!isRecentAdded) {
                onAllFileListObserverCall?.invoke(files as MutableList<DataModel>)
            } else
                isRecentAdded = false
        })
//        checkAllPermissions()
        GlobalScope.launch(Dispatchers.Main) {
            dataViewModel.getAllFiles()
            dataViewModel.getRecentDocList()
            dataViewModel.getMyFiles()
            dataViewModel.getBookmarkDocList()
        }
    }

    var onAllFileListObserverCall: ((MutableList<DataModel>) -> Unit)? = null

    private fun preLoadBottomSheetNativeAd() {
        refreshPreLoadedNativeAd(
            utilsViewModel.isPremiumUser(),
            ExitBottomSheetDialog::class.java.name
        ) {
            preLoadedNativeAd = it
        }
    }

    private fun setupViewPagerBottomSheet(viewPager: ViewPager2) {
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(DashboardFragment())
        adapter.addFragment(SearchFragment())
        adapter.addFragment(SettingsFragment())
        viewPager.adapter = adapter
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                loadFiles()
            }
        }

    private fun loadFiles() {
        GlobalScope.launch(Dispatchers.Main) { dataViewModel.getAllFiles() }
    }

    suspend fun loadRecentFiles() {
        dataViewModel.getRecentDocList()
    }

    private fun isDrawerOpen(): Boolean {
        return binding.drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun setDrawerClickListeners() {
        binding.drawer.premiumBtn.setOnClickListener(drawerClickListener())
        binding.drawer.restorePurchaseBtn.setOnClickListener(drawerClickListener())
        binding.drawer.termBtn.setOnClickListener(drawerClickListener())
        binding.drawer.rateUsBtn.setOnClickListener(drawerClickListener())
        binding.drawer.shareBtn.setOnClickListener(drawerClickListener())
    }

    private fun drawerClickListener(): View.OnClickListener {
        return View.OnClickListener {
            closeDrawer()
            when (it) {
                binding.drawer.premiumBtn -> {
                    closeDrawer()
                    binding.root.snack("Coming soon")
                    // openActivity(SubscriptionActivity::class.java)
                }
                binding.drawer.restorePurchaseBtn -> {
                    closeDrawer()
                    binding.root.snack("Coming soon")
                    //openActivity(SubscriptionActivity::class.java)
                }
                binding.drawer.termBtn -> openUrl("https://itechsolutionapps.wordpress.com/")
                binding.drawer.rateUsBtn -> {
                    closeDrawer()
                    launchMarket()
                }
                binding.drawer.shareBtn -> {
                    closeDrawer()
                    shareApp()
                }
            }
        }
    }

    private fun launchMarket() {
        RateDialogNew(this).createRateUsDialog(false, sharedPref)
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
            var shareMessage = "\nHi! I Just checked this app in play store, You must try it out:\n\n"
            shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_title)))
        } catch (e: Exception) {}
    }

    override fun onBackPressed() {
        if (isDrawerOpen())
            closeDrawer()
        else {
            if (binding.viewPagerMain.currentItem == 0) {
                if (sharedPref.getBoolean(isRatingDoneFirstTime)) {
                    supportFragmentManager.let {
                        ExitBottomSheetDialog.newInstance(Bundle()).apply {
                            show(it, tag)
                        }
                    }
                } else {
                    RateDialogNew(this).createRateUsDialog(true, sharedPref)
                }
            } else binding.viewPagerMain.setCurrentItem(0, false)
        }
    }

    override fun onResume() {
        super.onResume()
        val cacheBitmap = MyCache.getInstance().retrieveBitmapFromCache(Constants.BITMAP_STICKER)
        if (cacheBitmap != null)
            MyCache.getInstance().retrieveBitmapFromCache(Constants.BITMAP_STICKER).recycle()

//        if(!utilsViewModel.checkPermission(this) || dataViewModel.getAllFilesForCheck().isEmpty()){
//            checkAllPermissions()
//        }

        if (!utilsViewModel.checkPermission(this)) {
            checkAllPermissions()
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                dataViewModel.getAllFiles()
                dataViewModel.getRecentDocList()
                dataViewModel.getMyFiles()
                dataViewModel.getBookmarkDocList()
            }
        }
    }

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.drawer.shimmerViewContainer.visibility = View.VISIBLE
        binding.drawer.shimmerViewContainer.startShimmer()

        if (!utilsViewModel.isPremiumUser() && isInternetConnected()) {
            binding.drawer.adLayout.visibility = View.VISIBLE
            setNativeAd(
                utilsViewModel.isPremiumUser(),
                binding.drawer.adLayout,
                R.layout.empty_screen_ad_layout,
                TAG,
                placement = NativeAdOptions.ADCHOICES_BOTTOM_RIGHT,
                adMobNativeId = getString(R.string.native_id), onFailed = {
                    binding.drawer.shimmerViewContainer.visibility = View.GONE
                    binding.drawer.adLayout.visibility = View.GONE
                    binding.drawer.shimmerViewContainer.stopShimmer()
                }
            ) {
                binding.drawer.shimmerViewContainer.visibility = View.GONE
                binding.drawer.shimmerViewContainer.stopShimmer()
            }
        } else {
            binding.drawer.shimmerViewContainer.visibility = View.GONE
            binding.drawer.adLayout.visibility = View.GONE
            binding.drawer.shimmerViewContainer.stopShimmer()
        }
        /************************/
    }

    private fun checkAllPermissions() {
        onPermissionFailed = {
            showToast("Permission must required")
        }
        onPermissionGranted = {
//            retrieveAllFiles()
            GlobalScope.launch(Dispatchers.Main) {
                dataViewModel.getAllFiles()
                dataViewModel.getRecentDocList()
                dataViewModel.getMyFiles()
                dataViewModel.getBookmarkDocList()
            }
        }
        onPermissionError = {
            showToast("There is some error while getting permission")
        }
        if (utilsViewModel.androidVersionIs11OrAbove()) {
            if (!utilsViewModel.checkPermission11()) {
                supportFragmentManager.let {
                    AllowPermissionBottomSheet.newInstance(Bundle()).apply {
                        show(it, tag)
                        mListener = object : AllowPermissionBottomSheet.ItemClickListener {
                            override fun onItemClick(item: String) {
                                if (item == Constants.ALLOW) {
                                    get11Permission()
                                } else if (item == Constants.CANCEL) {
                                    onPermissionFailed?.invoke()
                                }
                            }
                        }
                    }
                }
            } else {
                onPermissionGranted?.invoke()
            }
        } else {
            getStoragePermission(onPermissionGranted ={
                onPermissionGranted?.invoke()
            },onPermissionDenied = {
                onPermissionFailed?.invoke()
            })
        }
    }

    @SuppressLint("InlinedApi")
    fun get11Permission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
            someActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            someActivityResultLauncher.launch(intent)
        }
    }

    private var someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (utilsViewModel.checkPermission11()) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionFailed?.invoke()
                showToast("Permission must required to retrieve files")
            }
        }

    var onPermissionGranted: (() -> Unit)? = null
    var onPermissionFailed: (() -> Unit)? = null
    var onPermissionError: ((String) -> Unit)? = null

//    private fun retrieveAllFiles() {
//        GlobalScope.launch(Dispatchers.Main) {
//            progressDialog.show()
//        }.invokeOnCompletion {
//            dataViewModel.retrieveFilesFormDevice { allFilesFromDevice ->
//                GlobalScope.launch(Dispatchers.IO) {
//                    allFilesFromDevice.forEach {
//                        try {
//                            if (it.path.contains(Constants.PDF))
//                                if (documentUtils.isPDFEncrypted(it.path)) {
//                                    it.isLock = true
//                                }
//                            dataViewModel.addDocModel(it)
//                        } catch (e: Exception) {}
//                    }
//                }.invokeOnCompletion {
//                    GlobalScope.launch(Dispatchers.Main) {
//                        progressDialog.dismiss()
//                        dataViewModel.getAllFiles()
//                        dataViewModel.getRecentDocList()
//                        dataViewModel.getMyFiles()
//                        dataViewModel.getBookmarkDocList()
//                    }
//                }
//            }
//        }
//    }
}