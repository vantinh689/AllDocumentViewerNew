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
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.FragmentMyFilesBinding
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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Keep
class BookmarkFragment : BaseFragment(), MenuBottomSheet.ItemClickListener {

    private var param1: String? = null
    private var param2: String? = null

    private var dataModel: DataModel? = null
    private val TAG = "MyFilesFragment"

    lateinit var binding: FragmentMyFilesBinding

    private var filesList = mutableListOf<DataModel>()

    private lateinit var adapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyFilesBinding.inflate(layoutInflater)


        initView()
        setObservers()
        setListeners()
        return binding.root
    }


    private fun setListeners() {
        adapter.setOnItemClickListener { docModel ->
            activity?.let { activity ->
                if (Companions.pdfListCounter == Companions.globalInterAdCounter) {
                    this.dataModel = docModel
                    InterstitialAdsUtils.getInstance().showInterstitialAdNew(activity) {
                        activity.performViewPagerAdapterItemClickListeners(docModel,dataViewModel)
                    }
                    Companions.pdfListCounter = 0
                } else {
                    Companions.pdfListCounter++
                    activity.performViewPagerAdapterItemClickListeners(docModel,dataViewModel)
                }
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
            activity?.let { activity ->
                activity.supportFragmentManager.let {
                    MenuBottomSheet.newInstance(Bundle().apply {
                        putSerializable(Constants.DOC_MODEL, docModel)
                    }).apply {
                        show(it, tag)
                        mListener = this@BookmarkFragment
                    }
                }
            }
        }
    }

    private fun setObservers() {
        dataViewModel.dataModelBookmarkList.observe(viewLifecycleOwner, { list ->
            GlobalScope.launch(Dispatchers.Default) {
                GlobalScope.launch(Dispatchers.Main) {
                    filesList.clear()
                    binding.rv.recycledViewPool.clear()
                    adapter.notifyDataSetChanged()
                }.invokeOnCompletion {
                    GlobalScope.launch(Dispatchers.Default) {
                        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
                        if (!list.isNullOrEmpty()) {
                            filesList = list as MutableList<DataModel>
                            GlobalScope.launch(Dispatchers.Main) {
                                adapter.updateLayout(
                                    newList = list, isGridEnable = gridEnable, false,
                                    isInternetConnected = false,
                                    sortBy = Companions.sortType
                                )
                            }.invokeOnCompletion {
                                GlobalScope.launch(Dispatchers.Main) {
                                    binding.noDataFoundLayout.noDataFoundLayout.visibility =
                                        View.GONE
                                    if (list.size <= 1) {
                                        binding.noDataFoundLayout.noDataFoundLayout.visibility =
                                            View.VISIBLE
                                        binding.noDataFoundLayout.imageView4.visibility =
                                            View.INVISIBLE
                                        binding.noDataFoundLayout.textView7.visibility =
                                            View.INVISIBLE
                                        refreshAdOnView()
                                    } else {
                                        binding.noDataFoundLayout.noDataFoundLayout.visibility =
                                            View.GONE
                                    }
                                }
                            }
                        } else {
                            GlobalScope.launch(Dispatchers.Main) {
                                binding.noDataFoundLayout.noDataFoundLayout.visibility =
                                    View.VISIBLE
                                refreshAdOnView()
                                adapter.updateLayout(
                                    mutableListOf(), isGridEnable = gridEnable, false,
                                    isInternetConnected = false,
                                    sortBy = Companions.sortType
                                )
                            }
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    private fun initView() {
        if (preLoadedNativeAd == null)
            activity?.refreshPreLoadedNativeAd(
                utilsViewModel.isPremiumUser(),
                ExitBottomSheetDialog::class.java.name
            ) {
                preLoadedNativeAd = it
            }
        binding.progressBar.visibility = View.VISIBLE

        val gridEnable = sharedPref.getBoolean(Constants.GRID_VALUE)
        adapter = if (activity is DashboardActivity) {
            FileListAdapter(gridEnable,Constants.BOOKMARK)
        } else {
            FileListAdapter(gridEnable, Constants.SEARCH)
        }
        updateLayoutManager(gridEnable)
    }

    private fun checkList(list: List<DataModel>) {
        if (!list.isNullOrEmpty()) {
            binding.noDataFoundLayout.noDataFoundLayout.visibility = View.VISIBLE
            refreshAdOnView()
        } else
            binding.noDataFoundLayout.noDataFoundLayout.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun refreshAdOnView() {
        /**show native ad*/
        binding.noDataFoundLayout.shimmerViewContainer.visibility = View.VISIBLE
        binding.noDataFoundLayout.shimmerViewContainer.startShimmer()

        context?.let { context ->
            if (!utilsViewModel.isPremiumUser() && context.isInternetConnected()) {
                binding.noDataFoundLayout.adLayout.visibility = View.VISIBLE
                activity?.setNativeAd(
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
            }else{
                binding.noDataFoundLayout.shimmerViewContainer.visibility = View.GONE
                binding.noDataFoundLayout.adLayout.visibility = View.GONE
                binding.noDataFoundLayout.shimmerViewContainer.stopShimmer()
            }
        }
        /************************/
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookmarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: String, dataModel: DataModel) {
        context?.showMenuBtnBottomSheet(binding.root, item, dataModel, dataViewModel)
    }
}