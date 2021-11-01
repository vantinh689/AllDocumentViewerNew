package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.skydoves.powermenu.PowerMenuItem
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentDocumentsBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.FileListAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.AllowPermissionBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.MenuBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.performViewPagerAdapterItemClickListeners
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showMenuBtnBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showSortingDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DocumentsActivity : BaseActivity(), MenuBottomSheet.ItemClickListener {

    lateinit var binding: FragmentDocumentsBinding
    private lateinit var adapter: FileListAdapter
    private var filesType: String = ""
    private var filesList = mutableListOf<DataModel>()
    private val gridEnable: Boolean = false

    private var nativeAd: Any? = null

    private var tabCurrentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this fragment
        binding = FragmentDocumentsBinding.inflate(layoutInflater)

        initView()
        setListeners()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if(Companions.isChangeFromOtherModuleDone){
            adapter.notifyDataSetChanged()
            Companions.isChangeFromOtherModuleDone = false
        }
    }

    private val TAG = "DocumentsFragment"

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.VISIBLE
        binding.noDataFoundLayout.shimmerViewContainer.startShimmer()

        if (!utilsViewModel.isPremiumUser() && isInternetConnected()) {
            binding.noDataFoundLayout.adLayout.visibility = View.VISIBLE
            setNativeAd(
                utilsViewModel.isPremiumUser(),
                binding.noDataFoundLayout.adLayout,
                R.layout.empty_screen_ad_layout,
                placement = NativeAdOptions.ADCHOICES_BOTTOM_RIGHT,
                TAG = TAG,
                preLoadedNativeAd = nativeAd,
                adMobNativeId = getString(R.string.searchScreenNativeId),
                onFailed = {
                    binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                    binding.noDataFoundLayout.adLayout.visibility = View.GONE
                    binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                }
            ) {
                binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                nativeAd = it
            }
        } else {
            binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
            binding.noDataFoundLayout.adLayout.visibility = View.GONE
            binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
        }
        /************************/
    }

    private fun setListeners() {
        adapter.setOnItemClickListener { docModel ->
            addButtonDelay(1000)
            if (Companions.pdfListCounter == Companions.globalInterAdCounter) {
                InterstitialAdsUtils.getInstance().showInterstitialAdNew(this) {
                    performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                }
                Companions.pdfListCounter = 0
            } else {
                Companions.pdfListCounter++
                performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
            }
        }
        adapter.setOnBookmarkClickListener { path: String, isBookmark: Boolean, docModel ->
            addButtonDelay(1000)
            GlobalScope.launch(Dispatchers.Default) {
//                mainViewModel.addBookmark(path, !isBookmark)
                if (dataViewModel.findDataModel(docModel.path).isEmpty()) {
                    docModel.isBookmarked = true
                    dataViewModel.addDataModel(docModel)
                } else {
                    dataViewModel.addBookmark(path, !isBookmark)
                }
            }
        }
        adapter.setOnMenuClickListener { docModel ->

            supportFragmentManager.let {
                MenuBottomSheet.newInstance(Bundle().apply {
                    putSerializable(Constants.DOC_MODEL, docModel)
                    putBoolean(Constants.IS_FROM_HOME_SCREEN, false)
                }).apply {
                    show(it, tag)
                    mListener = this@DocumentsActivity
                }
            }
        }

        binding.searchLayout.searchEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(keyword: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val newList = mutableListOf<DataModel>()
                GlobalScope.launch(Dispatchers.IO) {
                    filesList.forEach {
                        if (it.name.toLowerCase().contains(keyword.toString().toLowerCase()))
                            newList.add(it)
                    }
                }.invokeOnCompletion {
                    GlobalScope.launch(Dispatchers.Main) {
                        updateList(newList)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        binding.searchLayout.micBtn.setOnClickListener {
            mic()
        }
        binding.header.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.searchLayout.sortBtn.setOnClickListener {
            addButtonDelay(500)
            showSortingDialog(it,
                onResult = { i: Int, _: PowerMenuItem ->
                    Log.d("TAG123", "outer123444")
                    when (i) {
                        0 -> {
                            Companions.sortType = Constants.DATE
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(filesList)
                            }
                        }
                        1 -> {
                            Companions.sortType = Constants.NAME
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(filesList)
                            }
                        }
                        2 -> {
                            Companions.sortType = Constants.SIZE
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(filesList)
                            }
                        }
                    }
                },
                onDismissResult = {})
        }

        binding.wordTab.setOnClickListener {
            if(tabCurrentPosition!=1) {
                setSelectedTapPosition(1)
                filesList = dataViewModel.docFiles
                updateList(dataViewModel.docFiles)
            }
        }
        binding.excelTab.setOnClickListener {
            if(tabCurrentPosition!=3) {
                setSelectedTapPosition(3)
                filesList = dataViewModel.excelFiles
                updateList(dataViewModel.excelFiles)
            }
        }
        binding.pptTab.setOnClickListener {
            if(tabCurrentPosition!=2) {
                setSelectedTapPosition(2)
                filesList = dataViewModel.pptFiles
                updateList(dataViewModel.pptFiles)
            }
        }
        binding.pdfTab.setOnClickListener {
            if(tabCurrentPosition!=0) {
                setSelectedTapPosition(0)
                filesList = dataViewModel.pdfFiles
                updateList(dataViewModel.pdfFiles)
            }
        }
    }

    private fun mic() {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB")
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
        try {
            startActivityForResult(sttIntent, Constants.REQUEST_CODE_STT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            binding.root.snack("Your device does not support STT.")
        }
    }

    private fun initView() {
        binding.progressBar.visibility = View.VISIBLE
        // showKeyboardOnView(binding.searchLayout.searchEdt)
        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
        intent.extras?.let {
            filesType = it.getString(Constants.TYPE).toString()
        }
        when (filesType) {
            Constants.PDF -> {
                setSelectedTapPosition(0)
            }
            Constants.PPT -> {
                setSelectedTapPosition(2)
            }
            Constants.docExtension -> {
                setSelectedTapPosition(1)
            }
            Constants.excelExtension -> {
                setSelectedTapPosition(3)
            }
            Constants.AllFiles -> {
                binding.header.headerTxt.text = "All Files"
                binding.tabLayout.visibility = View.GONE
            }
        }
        adapter = FileListAdapter(gridEnable, Constants.SEARCH)
        updateLayoutManager(gridEnable)
//        mainViewModel.retrieveFilesFormDevice {
//            updateList()
//        }
        checkAllPermissions()
    }

    private fun setSelectedTapPosition(position: Int) {
        tabCurrentPosition = position
        binding.apply {
            when (position) {
                0 -> {
                    pdfTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.colorTool2
                    )
                    pptTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    excelTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    wordTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    binding.header.headerTxt.text = "Pdf Files"
                }
                1 -> {
                    binding.header.headerTxt.text = "Word Files"
                    pdfTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    pptTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    excelTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    wordTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.colorTool3
                    )
                }
                2 -> {
                    binding.header.headerTxt.text = "Ppt Files"
                    pdfTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    pptTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.colorTool4
                    )
                    excelTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    wordTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                }
                3 -> {
                    binding.header.headerTxt.text = "Excel Files"
                    pdfTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    pptTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                    excelTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.colorTool5
                    )
                    wordTab.backgroundTintList = AppCompatResources.getColorStateList(
                        this@DocumentsActivity,
                        R.color.transparent
                    )
                }
            }
        }
    }

    private fun initList() {
        when (filesType) {
            Constants.PDF -> filesList = dataViewModel.pdfFiles
            Constants.PPT -> filesList = dataViewModel.pptFiles
            Constants.docExtension -> filesList = dataViewModel.docFiles
            Constants.excelExtension -> filesList = dataViewModel.excelFiles
            Constants.AllFiles -> filesList = dataViewModel.allDeviceFiles
        }
        updateList(filesList)
    }

    private fun updateList(filesList: MutableList<DataModel>) {
        GlobalScope.launch(Dispatchers.Main) {
            checkList(filesList)
            binding.progressBar.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
            binding.rv.recycledViewPool.clear()
            adapter.notifyDataSetChanged()
        }.invokeOnCompletion {
            adapter.updateLayout(
                newList = filesList,
                isGridEnable = gridEnable,
                utilsViewModel.isPremiumUser(),
                isInternetConnected(),
                Companions.sortType
            ) {
                binding.progressBar.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
            }
        }
    }

    private fun checkList(list: MutableList<DataModel>) {
        when {
            list.isEmpty() -> {
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.VISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.VISIBLE
                refreshAdOnView()
            }
            list.size <= 1 -> {
                refreshAdOnView()
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.INVISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.INVISIBLE
            }
            else -> binding.noDataFoundLayout.noDataFoundLayout.visibility = View.GONE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun updateLayoutManager(isGridEnable: Boolean) {
        if (!isGridEnable) {
            binding.rv.layoutManager = GridLayoutManager(this, 1)
        } else {
            val layoutManager = GridLayoutManager(this, 2)
            binding.rv.layoutManager = layoutManager
        }
        binding.rv.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Constants.REQUEST_CODE_STT -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        if (!result.isNullOrEmpty()) {
                            val recognizedText = result[0]
//                            showToast(recognizedText)
                            binding.searchLayout.searchEdt.setText(recognizedText)
                            if (binding.searchLayout.searchEdt.text.toString().isNotEmpty()) {
                                binding.searchLayout.searchEdt.setSelection(binding.searchLayout.searchEdt.text.toString().length)
                            }
                        }
                    }
                }
            }
        }
    }

    var onPermissionGranted: (() -> Unit)? = null
    var onPermissionFailed: (() -> Unit)? = null
    var onPermissionError: ((String) -> Unit)? = null

    private fun checkAllPermissions() {
        onPermissionFailed = {
            dataViewModel.retrieveFilesFormDevice {
                initList()
            }
            binding.root.snack("Please give permission to continue")
        }
        onPermissionGranted = {
            dataViewModel.retrieveFilesFormDevice {
                initList()
            }
        }
        onPermissionError = {
            dataViewModel.retrieveFilesFormDevice {
                initList()
            }
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
//            utilsViewModel.checkStoragePermission(this, onPermissionsGranted = {
//                onPermissionGranted?.invoke()
//            }, onPermissionsDenied = {
//                onPermissionFailed?.invoke()
//            }, onError = {
//                onPermissionError?.invoke(it)
//                this.showToast(it)
//            })

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
            intent.data = Uri.parse(String.format("package:%s", packageName))
            someActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            someActivityResultLauncher.launch(intent)
        }
    }

    private var someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (utilsViewModel.checkPermission11()) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionFailed?.invoke()
                showToast("Permission must required to retrieve files")
            }
        }

    override fun onItemClick(item: String, dataModel: DataModel) {
        showMenuBtnBottomSheet(binding.root, item, dataModel, dataViewModel) {
            dataViewModel.retrieveRefreshedFilesFormDevice {
                updateList(it)
            }
        }
    }

}