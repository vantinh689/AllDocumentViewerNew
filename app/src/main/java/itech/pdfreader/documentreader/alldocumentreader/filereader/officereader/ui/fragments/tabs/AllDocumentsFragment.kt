package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAdOptions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.refreshPreLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentAllPdfBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DashboardActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.FileListAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.PdfDocumentAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.BaseFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.ExitBottomSheetDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.MenuBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.performViewPagerAdapterItemClickListeners
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showMenuBtnBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.globalInterAdCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.pdfListCounter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.preLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.sortType
import kotlinx.coroutines.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AllDocumentsFragment : BaseFragment(), MenuBottomSheet.ItemClickListener {

    private var docModel: DataModel? = null
    private var gridEnable: Boolean = false
    private var filesList = mutableListOf<DataModel>()

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentAllPdfBinding
    private lateinit var adapter: FileListAdapter

    private val TAG = "AllPdfFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllPdfBinding.inflate(layoutInflater)
        binding.apply {
            progressBar.visibility = View.VISIBLE
        }

        initView()
        setListeners()
        return binding.root
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    private fun setListeners() {
        adapter.setOnItemClickListener { docModel ->
            activity?.let { activity ->
                if (pdfListCounter == globalInterAdCounter) {
                    this.docModel = docModel
                    InterstitialAdsUtils.getInstance()
                        .showInterstitialAdNew(activity) {
                            activity.performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                        }
                    pdfListCounter = 0
                } else {
                    pdfListCounter++
                    activity.performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                }
            }
        }
        adapter.setOnBookmarkClickListener { path: String, isBookmark: Boolean, docModel ->
            activity?.addButtonDelay(1000)
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
            activity?.addButtonDelay(1000)
            activity?.let { activity ->
                activity.supportFragmentManager.let {
                    MenuBottomSheet.newInstance(Bundle().apply {
                        putSerializable(Constants.DOC_MODEL, docModel)
                    }).apply {
                        show(it, tag)
                        mListener = this@AllDocumentsFragment
                    }
                }
            }
        }
    }

    private fun updateLayoutManager(isGridEnable: Boolean) {
        if (!isGridEnable) {
            binding.allFilesRv.layoutManager = GridLayoutManager(context, 1)
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.allFilesRv.layoutManager = layoutManager
        }
        binding.allFilesRv.adapter = adapter
    }

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.VISIBLE
        binding.noDataFoundLayout.shimmerViewContainer.startShimmer()

        context?.let { context ->
            if (!utilsViewModel.isPremiumUser() && context.isInternetConnected()) {
                binding.noDataFoundLayout.adLayout.visibility = View.VISIBLE
                activity?.let { activity ->
                    activity.setNativeAd(
                        utilsViewModel.isPremiumUser(),
                        binding.noDataFoundLayout.adLayout,
                        R.layout.empty_screen_ad_layout,
                        placement = NativeAdOptions.ADCHOICES_BOTTOM_RIGHT,
                        preLoadedNativeAd = preLoadedNativeAd,
                        TAG = TAG,
                        adMobNativeId = getString(R.string.native_id), onFailed = {
                            binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                            binding.noDataFoundLayout.adLayout.visibility = View.GONE
                            binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                        }
                    ) {
                        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                        binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                    }
                }
            }else{
                binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                binding.noDataFoundLayout.adLayout.visibility = View.GONE
                binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
    }

    private fun initView() {

        binding.progressBar.visibility = View.VISIBLE

        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
        adapter = if (activity is DashboardActivity) {
            FileListAdapter(gridEnable)
        } else {
            FileListAdapter(gridEnable, Constants.SEARCH)
        }
        updateLayoutManager(gridEnable)

        binding.progressBar.visibility = View.VISIBLE
        binding.allFilesRv.visibility = View.GONE

        dataViewModel.isObserverChangeAll.observe(viewLifecycleOwner, {
            updateList(dataViewModel.allDeviceFiles)
        })
    }

    private fun checkList(list: List<DataModel>) {
        when {
            list.isEmpty() -> {
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.VISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.VISIBLE
                binding.allFilesRv.visibility = View.GONE
                refreshAdOnView()
            }
            list.size <= 1 -> {
                refreshAdOnView()
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
                binding.noDataFoundLayout.imageView4.visibility = View.INVISIBLE
                binding.noDataFoundLayout.textView7.visibility = View.INVISIBLE
                binding.allFilesRv.visibility = View.VISIBLE
            }
            else -> {
                binding.noDataFoundLayout.noDataFoundLayout.visibility = View.GONE
                binding.allFilesRv.visibility = View.VISIBLE
            }
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun updateList(filesList: MutableList<DataModel>) {
        context?.let { context ->
            checkList(filesList)
            binding.progressBar.visibility = View.VISIBLE
            binding.allFilesRv.visibility = View.GONE

            adapter.updateLayout(
                newList = filesList,
                isGridEnable = false,
                utilsViewModel.isPremiumUser(),
                context.isInternetConnected(),
                sortType
            ) {
                binding.progressBar.visibility = View.GONE
                binding.allFilesRv.visibility = View.VISIBLE
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllDocumentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: String, docModel: DataModel) {
        context?.showMenuBtnBottomSheet(
            binding.root,
            item,
            docModel,
            dataViewModel
        )
    }
}
