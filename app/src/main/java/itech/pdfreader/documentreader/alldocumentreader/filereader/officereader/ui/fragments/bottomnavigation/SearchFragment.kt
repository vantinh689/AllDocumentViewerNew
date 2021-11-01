package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomnavigation

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.skydoves.powermenu.PowerMenuItem
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.InterstitialAdsUtils
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.refreshPreLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentSearchBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.FileListAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.BaseFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.AllowPermissionBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.MenuBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.performViewPagerAdapterItemClickListeners
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showMenuBtnBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showSortingDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SearchFragment : BaseFragment(), MenuBottomSheet.ItemClickListener {
    lateinit var binding: FragmentSearchBinding

    private lateinit var adapter: FileListAdapter

    private var filesList = mutableListOf<DataModel>()
    private val gridEnable: Boolean = false

    private var nativeAd: Any? = null

    private var docModel: DataModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater)
        initView()
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        adapter.setOnItemClickListener {docModel->
            activity?.apply {
                if (Companions.pdfListCounter == Companions.globalInterAdCounter) {
                    this@SearchFragment.docModel = docModel
                    InterstitialAdsUtils.getInstance().showInterstitialAdNew(this) {
                        performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                    }
                    Companions.pdfListCounter = 0
                } else {
                    Companions.pdfListCounter++
                    performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                }
            }
        }
        adapter.setOnBookmarkClickListener { path: String, isBookmark: Boolean, docModel ->
            GlobalScope.launch(Dispatchers.Default) {
//                mainViewModel.addBookmark(id, !isBookmark)
                if (dataViewModel.findDataModel(docModel.path).isEmpty()) {
                    docModel.isBookmarked = true
                    dataViewModel.addDataModel(docModel)
                } else {
                    dataViewModel.addBookmark(path, !isBookmark)
                }
            }
        }
        adapter.setOnMenuClickListener { docModel ->
            activity?.supportFragmentManager?.let {
                MenuBottomSheet.newInstance(Bundle().apply {
                    putSerializable(Constants.DOC_MODEL, docModel)
                }).apply {
                    show(it, tag)
                    mListener = this@SearchFragment
                }
            }
        }
        binding.searchLayout.searchEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(keyword: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val newList = mutableListOf<DataModel>()
                GlobalScope.launch(Dispatchers.IO) {
                    fileList.forEach {
                        if (it.name.toLowerCase(Locale.ROOT).contains(
                                keyword.toString().toLowerCase(
                                    Locale.ROOT
                                )
                            )
                        )
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
        binding.searchLayout.sortBtn.setOnClickListener {
            activity?.addButtonDelay(500)
            (activity as AppCompatActivity).showSortingDialog(it,
                onResult = { i: Int, _: PowerMenuItem ->
                    Log.d("TAG123", "outer123444")
                    when (i) {
                        0 -> {
                            Companions.sortType = Constants.DATE
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(it)
                            }
                        }
                        1 -> {
                            Companions.sortType = Constants.NAME
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(it)
                            }
                        }
                        2 -> {
                            Companions.sortType = Constants.SIZE
                            dataViewModel.retrieveFilesFormDevice {
                                updateList(it)
                            }
                        }
                    }
                },
                onDismissResult = {})
        }
        binding.searchLayout.micBtn.setOnClickListener {
            mic()
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

    override fun onPause() {
        super.onPause()
        GlobalScope.launch(Dispatchers.Main) { dataViewModel.getSearchResults("") }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch(Dispatchers.Main) { dataViewModel.getSearchResults("") }
    }

    private fun initView() {
        activity?.refreshPreLoadedNativeAd(utilsViewModel.isPremiumUser(), TAG) {
            nativeAd = it
        }

        binding.progressBar.visibility = View.VISIBLE

        showKeyboardOnView(binding.searchLayout.searchEdt)
        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
        FileListAdapter(gridEnable, Constants.SEARCH)

        binding.progressBar.visibility = View.VISIBLE
        binding.rv.visibility = View.GONE

        checkAllPermissions()

        adapter = FileListAdapter(gridEnable, Constants.SEARCH)
        updateLayoutManager(gridEnable)
        dataViewModel.isObserverChangeAll.observe(viewLifecycleOwner, {
            dataViewModel.retrieveFilesFormDevice {
                fileList = it
                updateList(dataViewModel.allDeviceFiles)
            }
        })

    }

    private var fileList = mutableListOf<DataModel>()

    private fun checkList(list: List<DataModel>) {
        when {
            list.isEmpty() -> {
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.VISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                refreshAdOnView()
            }
            list.size <= 1 -> {
                refreshAdOnView()
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.INVISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.INVISIBLE
                binding.rv.visibility = View.VISIBLE
            }
            else -> {
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
            }
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun updateList(filesList: MutableList<DataModel>) {
        context?.let { context ->
            checkList(filesList)
            binding.progressBar.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
            adapter.updateLayout(
                newList = filesList,
                isGridEnable = false,
                utilsViewModel.isPremiumUser(),
                context.isInternetConnected(),
                Companions.sortType
            ) {
                binding.progressBar.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
            }
        }
    }

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.VISIBLE
        binding.noDataFoundLayout.shimmerViewContainer.startShimmer()

        activity?.let { context ->
            if (!utilsViewModel.isPremiumUser() && context.isInternetConnected()) {
                binding.noDataFoundLayout.adLayout.visibility = View.VISIBLE
                context.setNativeAd(
                    utilsViewModel.isPremiumUser(),
                    binding.noDataFoundLayout.adLayout,
                    R.layout.empty_screen_ad_layout,
                    placement = NativeAdOptions.ADCHOICES_BOTTOM_RIGHT,
                    preLoadedNativeAd = nativeAd,
                    TAG = TAG,
                    adMobNativeId = getString(R.string.native_id), onFailed = {
                        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                        binding.noDataFoundLayout.adLayout.visibility = View.GONE
                        binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                    }
                ) {
                    nativeAd = it
                    binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                    binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                }
            }else{
                binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                binding.noDataFoundLayout.adLayout.visibility = View.GONE
                binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
    }

    private val TAG = "SearchActivity"

    private fun updateLayoutManager(isGridEnable: Boolean) {
        if (!isGridEnable) {
            binding.rv.layoutManager = GridLayoutManager(context, 1)
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rv.layoutManager = layoutManager
        }
        binding.rv.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
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
            GlobalScope.launch(Dispatchers.Main) {
                dataViewModel.getAllFiles()
                dataViewModel.getRecentDocList()
                dataViewModel.getMyFiles()
                dataViewModel.getBookmarkDocList()
            }
            binding.root.snack("Please give permission to continue")
        }
        onPermissionGranted = {
            GlobalScope.launch(Dispatchers.Main) {
                dataViewModel.getAllFiles()
                dataViewModel.getRecentDocList()
                dataViewModel.getMyFiles()
                dataViewModel.getBookmarkDocList()
            }
        }
        onPermissionError = {
            GlobalScope.launch(Dispatchers.Main) {
                dataViewModel.getAllFiles()
                dataViewModel.getRecentDocList()
                dataViewModel.getMyFiles()
                dataViewModel.getBookmarkDocList()
            }
        }
        if (utilsViewModel.androidVersionIs11OrAbove()) {
            if (!utilsViewModel.checkPermission11()) {
                activity?.supportFragmentManager?.let {
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
            context?.let {
                utilsViewModel.checkStoragePermission(it, onPermissionsGranted = {
                    onPermissionGranted?.invoke()
                }, onPermissionsDenied = {
                    onPermissionFailed?.invoke()
                }, onError = {
                    onPermissionError?.invoke(it)
                    activity?.showToast("$it")
                })
            }
        }
    }

    @SuppressLint("InlinedApi")
    fun get11Permission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", context?.packageName))
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
                context?.showToast("Permission must required to retrieve files")
            }
        }

    override fun onItemClick(item: String, dataModel: DataModel) {
        context?.showMenuBtnBottomSheet(binding.root, item, dataModel, dataViewModel)
    }
}