package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.tabs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.skydoves.powermenu.PowerMenuItem
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.refreshPreLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.setNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.utilities.ExtentionsFunctions.isInternetConnected
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentRecentFilesBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.activities.DashboardActivity
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters.FileListAdapter
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.BaseFragment
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.ExitBottomSheetDialog
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.fragments.bottomsheats.MenuBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.performViewPagerAdapterItemClickListeners
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.showMenuBtnBottomSheet
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.preLoadedNativeAd
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RecentFilesFragment : BaseFragment(), MenuBottomSheet.ItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var docModel: DataModel? = null
    private val TAG = "RecentFilesFragment"

    lateinit var binding: FragmentRecentFilesBinding

    private var filesList = mutableListOf<DataModel>()

    private lateinit var adapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecentFilesBinding.inflate(layoutInflater)

        initView()
        setObservers()
        setListeners()


        return binding.root
    }


    private fun setListeners() {
        activity?.let { activity ->
            adapter.setOnItemClickListener { docModel ->
                if (Companions.pdfListCounter == Companions.globalInterAdCounter) {
                    this.docModel = docModel
                    InterstitialAdsUtils.getInstance().showInterstitialAdNew(activity) {
                        activity.performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                    }
                    Companions.pdfListCounter = 0
                } else {
                    Companions.pdfListCounter++
                    activity.performViewPagerAdapterItemClickListeners(docModel, dataViewModel)
                }
            }

            adapter.setOnBookmarkClickListener { path: String, isBookmark: Boolean, docModel ->
                GlobalScope.launch(Dispatchers.Default) {
                    if (dataViewModel.findDataModel(docModel.path).isEmpty()) {
                        docModel.isBookmarked = true
                        dataViewModel.addDataModel(docModel)
                    } else {
                        dataViewModel.addBookmark(path, !isBookmark)
                    }
                }
            }

            adapter.setOnMenuClickListener { docModel ->
                activity.supportFragmentManager.let {
                    MenuBottomSheet.newInstance(Bundle().apply {
                        putSerializable(Constants.DOC_MODEL, docModel)
                    }).apply {
                        show(it, tag)
                        mListener = this@RecentFilesFragment
                    }
                }
            }
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

    private fun updateList(list: MutableList<DataModel>) {
        GlobalScope.launch(Dispatchers.Default) {
            GlobalScope.launch(Dispatchers.Main) {
//                list.forEach {
//                    if (it.type.toLowerCase(Locale.ROOT) == Constants.PDF)
//                        documentUtils.isPDFEncrypted(it.path) { isEnc ->
//                            if (isEnc) {
//                                it.isLock = true
//                            }
//                        }
//                }
                binding.rv.recycledViewPool.clear()
            }.invokeOnCompletion {
                GlobalScope.launch(Dispatchers.Default) {
                    val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
                    if (!list.isNullOrEmpty()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            if (getModuleType() != Constants.MERGE)
                                context?.isInternetConnected()?.let { context ->
                                    adapter.updateLayout(
                                        newList = list,
                                        isGridEnable = gridEnable,
                                        utilsViewModel.isPremiumUser(),
                                        context,
                                        Companions.sortType
                                    )
                                }
                            else {
                                adapter.updateLayout(
                                    newList = list, isGridEnable = gridEnable, false,
                                    isInternetConnected = false,
                                    sortBy = Companions.sortType
                                )
                            }
                        }.invokeOnCompletion {
                            GlobalScope.launch(Dispatchers.Main) {
                                checkList(list)
                            }
                        }
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            checkList(mutableListOf())
                        }
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

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

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.VISIBLE
        binding.noDataFoundLayout.shimmerViewContainer.startShimmer()

        context?.let { context ->
            activity?.let { activity ->
                if (!utilsViewModel.isPremiumUser() && context.isInternetConnected()) {
                    binding.noDataFoundLayout.adLayout.visibility = View.VISIBLE
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
                        preLoadedNativeAd = it
                        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                        binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                    }
                } else {
                    binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                    binding.noDataFoundLayout.adLayout.visibility = View.GONE
                    binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
                }
            }
        }
        /************************/
    }

    private fun setObservers() {
        dataViewModel.dataModelRecentList.observe(viewLifecycleOwner, { list ->
            filesList = list as MutableList<DataModel>
            updateList(list)
        })
    }

    private fun initView() {
        binding.noDataFoundLayout.textView7.text = "No Recents Found"
        binding.progressBar.visibility = View.VISIBLE
        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
        adapter = if (activity is DashboardActivity) {
            FileListAdapter(gridEnable)
        } else {
            FileListAdapter(gridEnable, Constants.SEARCH)
        }
        updateLayoutManager(gridEnable)
    }

    private fun updateLayoutManager(isGridEnable: Boolean) {
        if (!isGridEnable) {
            binding.rv.layoutManager = GridLayoutManager(context, 1)
        } else {
            val layoutManager = GridLayoutManager(context, 2)
            binding.rv.layoutManager = layoutManager
        }
        binding.rv.adapter = adapter
    }

    override fun onItemClick(item: String, docModel: DataModel) {
        context?.showMenuBtnBottomSheet(binding.root, item, docModel, dataViewModel)
    }
}