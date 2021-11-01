/*package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdViewHolder
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdsCallBack
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.HeaderListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.PdfGridListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.PdfListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DocModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions.Companion.selectedDocModelPosition
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import kotlinx.coroutines.*
import java.util.*

class PdfListAdapter(private var isGridEnable: Boolean? = true, var type: String = "") :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val nativeAd: Any? = null

    private var currentList = mutableListOf<DocModel>()

    private var nativeAdsList = mutableListOf<NativeAd>()

    private fun setCurrentList(currentList: MutableList<DocModel>) {
        this.currentList = currentList
    }

    private var nativePositionArray = mutableListOf<Int>()

    init {
        setHasStableIds(true)
    }

    inner class MyViewHolder(private val itemViewBinding: PdfListItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(docModel: DocModel) {
            itemViewBinding.mergeCountTxt.visibility = View.GONE

            when (docModel.itemType) {
                Constants.MERGE_SELECTED -> {
                    itemViewBinding.apply {
                        menuBtn.visibility = View.GONE
                        if (docModel.isSelected) {
                            position = ""
                            mergeCountTxt.visibility = View.VISIBLE
                        } else
                            mergeCountTxt.visibility = View.GONE
                    }
                }
            }
            when (type) {
                Constants.SEARCH -> {
                    itemViewBinding.menuBtn.visibility = View.GONE
                }
                Constants.BOOKMARK -> {
                    itemViewBinding.menuBtn.visibility = View.GONE
                    itemViewBinding.bookmarkBtn.visibility = View.VISIBLE
                }
                Constants.MERGE -> {
                    itemViewBinding.apply {
                        menuBtn.visibility = View.GONE
                        mergeCountTxt.visibility = View.VISIBLE
                        cardView.setBackgroundColor(
                            ResourcesCompat.getColor(
                                itemViewBinding.root.resources,
                                R.color.backgroundColor,
                                null
                            )
                        )
                        position = (adapterPosition + 1).toString()
                    }
                }
            }
            itemViewBinding.apply {
                this.docModel = docModel
            }
            itemViewBinding.root.setOnClickListener {
                selectedDocModelPosition = adapterPosition
                onItemClickListener?.let {
                    it(docModel)
                }
            }
            itemViewBinding.bookmarkBtn.setOnClickListener {
                onBookmarkClickListener?.let {
                    it(docModel.id, docModel.isBookmarked)
                }
            }
            itemViewBinding.menuBtn.setOnClickListener {
                onMenuClickListener?.let {
                    it(docModel)
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].itemType) {
            Constants.NATIVE_AD -> 1
            else -> 0
        }
    }

    inner class MyGridViewHolder(private val itemViewBinding: PdfGridListItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(docModel: DocModel) {
            when (type) {
                Constants.SEARCH -> {
                    itemViewBinding.menuBtn.visibility = View.GONE
                }
                Constants.BOOKMARK -> {
                    itemViewBinding.menuBtn.visibility = View.GONE
                    itemViewBinding.bookmarkBtn.visibility = View.VISIBLE
                }
            }
            when (docModel.itemType) {
                Constants.MERGE_SELECTED -> {
                    itemViewBinding.apply {
                        itemViewBinding.menuBtn.visibility = View.GONE
                        itemViewBinding.mergeCountTxt.visibility = View.VISIBLE
                        position = docModel.pos
                    }
                }
            }
            itemViewBinding.apply {
                this.docModel = docModel
            }
            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(docModel)
                }
            }
            itemViewBinding.bookmarkBtn.setOnClickListener {
                onBookmarkClickListener?.let {
                    it(docModel.id, docModel.isBookmarked)
                }
            }
            itemViewBinding.menuBtn.setOnClickListener {
                onMenuClickListener?.let {
                    it(docModel)
                }
            }
        }
    }

    inner class MyHeaderViewHolder(private val itemViewBinding: HeaderListItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindView(name: String) {
            itemViewBinding.apply {
                this.headerName = name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                return if (isGridEnable == true) {
                    MyGridViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.pdf_grid_list_item,
                            parent,
                            false
                        )
                    )
                } else {
                    MyViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.pdf_list_item,
                            parent,
                            false
                        )
                    )
                }
            }
            1 -> {
                val view2 = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_native_ad_item, parent, false)
                return NativeAdViewHolder(context = parent.context, view2, R.string.native_id, object: NativeAdsCallBack{
                    override fun onNewAdLoaded(nativeAd: Any, position: Int) {
                        Log.d("List_nativee1111", "onNewAdLoaded()")
                        nativeAdsList.add(nativeAd as NativeAd)
                     }

                    override fun onAdClicked(position: Int) {
                        Log.d("List_nativee1111", "onAdClicked()")
                    }
                })
            }
            else -> {
                return if (isGridEnable == true) {
                    MyGridViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.pdf_grid_list_item,
                            parent,
                            false
                        )
                    )
                } else {
                    MyViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.pdf_list_item,
                            parent,
                            false
                        )
                    )
                }
            }
        }
    }

    private var onItemClickListener: ((DocModel) -> Unit)? = null
    private var onBookmarkClickListener: ((Int, Boolean) -> Unit)? = null
    private var onMenuClickListener: ((DocModel) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        if (isGridEnable == true) {
            if (holder is MyGridViewHolder)
                holder.bindView(item)
            else if (holder is NativeAdViewHolder)
                if (!nativePositionArray.contains(position)) {
                    holder.setData(nativeAd, position, 2)
                    nativePositionArray.add(position)
                }
        } else {
            if (holder is MyViewHolder)
                holder.bindView(item)
            else if (holder is NativeAdViewHolder) {
                if (!nativePositionArray.contains(position)) {
                    if (!nativeAdsList.isNullOrEmpty()) {
                        if (nativeAdsList.size > nativePositionArray.size) {
                            Log.d("List_nativee1111", "New Request sent fo ads")
                            holder.setData(nativeAdsList[nativeAdsList.size - 1], position, 2)
                            nativeAdsList.removeAt(nativeAdsList.size - 1)
                        }
                        else {
                            Log.d("List_nativee1111", "New Request sent fo ads")
                            nativePositionArray.add(position)
                            holder.setData(null, position, 2)
                        }
                    }else{
                        nativePositionArray.add(position)
                        holder.setData(null, position, 2)
                    }
                }
            }else{
                Log.d("List_nativee1111", "New Request sent fo ads")
            }
        }
    }

    fun setOnItemClickListener(listener: (DocModel) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnBookmarkClickListener(listener: (Int, Boolean) -> Unit) {
        onBookmarkClickListener = listener
    }

    fun setOnMenuClickListener(listener: (DocModel) -> Unit) {
        onMenuClickListener = listener
    }

    fun updateLayout(
        newList: MutableList<DocModel>,
        isGridEnable: Boolean,
        isPremiumUser: Boolean,
        isInternetConnected: Boolean,
        sortBy: String
    ) {
        GlobalScope.launch((Dispatchers.Default)) {
            try {
                for (i in newList.indices) {
                    if (i < newList.size)
                        if (newList[i].itemType == Constants.NATIVE_AD)
                            newList.removeAt(i)
                }
                when (sortBy) {
                    Constants.SIZE -> {
                        newList.sortByDescending { it.encryptedLength }
                    }
                    Constants.NAME -> {
                        newList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                    }
                    else -> {
                        newList.sortByDescending { it.encryptedTime }
                    }
                }
                this@PdfListAdapter.isGridEnable = isGridEnable
                if (!isPremiumUser && isInternetConnected) {
                    addNativeAdsInList(newList)
                }
                GlobalScope.launch((Dispatchers.Main)) {
                    currentList.clear()
                    currentList.addAll(newList)
                    setCurrentList(newList)
                    notifyDataSetChanged()
                }
            } catch (e: Exception) {
                GlobalScope.launch((Dispatchers.Main)) {
                    currentList.clear()
                    currentList.addAll(newList)
                    setCurrentList(newList)
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun addNativeAdsInList(newList: MutableList<DocModel>) {
        val nativeDocModel = DocModel()
        nativeDocModel.itemType = Constants.NATIVE_AD
        var adCounter = Constants.ADS_COUNT_AFTER + Constants.FIRST_ADS_COUNT_AFTER
        for (i in newList.indices) {
            if (i == Constants.FIRST_ADS_COUNT_AFTER || i == adCounter) {
                if (i < newList.size) {
                    if (newList[i].itemType != Constants.NATIVE_AD && newList[i - 1].itemType != Constants.NATIVE_AD) {
                        try {
                            newList.add(i, nativeDocModel)
                            if (i != Constants.FIRST_ADS_COUNT_AFTER)
                                adCounter += Constants.ADS_COUNT_AFTER
                        } catch (e: Exception) {

                        }
                    }
                }
            }
        }
    }
}*/
package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.adapters

import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.R
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdsViewHolderClass
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.adsutils.NativeAdCallback
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model.BaseItemModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ads.model.ItemTypeEnum
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.databinding.PdfListItemBinding
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Companions
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FileListAdapter(private var isGridEnable: Boolean? = true, var type: String = "") :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    NativeAdCallback {

    private var currentEntities: MutableList<BaseItemModel> = ArrayList()
    private var itemList = mutableListOf<DataModel>()
    private var selectedItems: SparseBooleanArray
    var adsHashMap = HashMap<Int, Any>()
    override fun onNewAdLoaded(nativeAd: Any, position: Int) {
        adsHashMap[position] = nativeAd
    }

    override fun onAdClicked(position: Int) {
        adsHashMap.remove(position)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentEntities[position].itemType() == ItemTypeEnum.REAL_ITEM.ordinal) {
            ItemTypeEnum.REAL_ITEM.ordinal
        } else {
            ItemTypeEnum.NATIVE_AD.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemTypeEnum.REAL_ITEM.ordinal -> {
                val binding: PdfListItemBinding = PdfListItemBinding.inflate(LayoutInflater.from(parent.context))
                DataViewHolder(binding)
            }
            else -> {
                val view2 = LayoutInflater.from(parent.context).inflate(R.layout.recycler_native_ad_item, parent, false)
                val nativeId= if(type==Constants.SEARCH){
                    R.string.searchScreenNativeId
                }else
                    R.string.documentListNativeId
                return NativeAdsViewHolderClass(context = parent.context, view2,nativeId, this)
            }
        }
    }

    fun setOnItemClickListener(listener: (DataModel) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnBookmarkClickListener(listener: (String, Boolean,DataModel) -> Unit) {
        onBookmarkClickListener = listener
    }

    fun setOnMenuClickListener(listener: (DataModel) -> Unit) {
        onMenuClickListener = listener
    }

    override fun getItemCount(): Int {
        return if (currentEntities.isEmpty())
            return 0
        else {
            currentEntities.size
        }
    }

    fun getImageEntitiesList(): List<DataModel> {
        return itemList
    }

    fun isSelected(position: Int): Boolean {
        return getSelectedItems().contains(position)
    }

    fun toggleSelection(position: Int) {
        if (selectedItems[position, false]) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    /**
     * Select all adapter items
     */
    fun selectAll() {
        if (itemCount == getSelectedItems().size) {
            selectedItems.clear()
        } else {
            for ((index, item) in currentEntities.withIndex()) {
                if (item is DataItemModel) {
                    selectedItems.put(index, true)
                }
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Clear the selection status for all items
     */
    fun clearSelection() {
        val selection = getSelectedItems()
        selectedItems.clear()
        for (i in selection) {
            notifyItemChanged(i)
        }
    }

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    val selectedItemCount: Int
        get() = selectedItems.size()

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    private fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun setSelectedItems(selectedItems: SparseBooleanArray) {
        this.selectedItems = selectedItems
    }

    fun clearItems() {
        currentEntities.clear()
    }

    fun bindView(dataModel: DataModel, itemViewBinding: PdfListItemBinding, adapterPosition: Int) {
        itemViewBinding.mergeCountTxt.visibility = View.GONE
        itemViewBinding.pdfTxt.isSelected = true

        when (dataModel.itemType) {
            Constants.MERGE_SELECTED -> {
                itemViewBinding.apply {
                    menuBtn.visibility = View.GONE
                    if (dataModel.isSelected) {
                        position = ""
                        mergeCountTxt.visibility = View.VISIBLE
                    } else
                        mergeCountTxt.visibility = View.GONE
                }
            }
        }
        when (type) {
            Constants.SEARCH -> {
//                itemViewBinding.menuBtn.visibility = View.GONE
            }
            Constants.BOOKMARK -> {
//                itemViewBinding.menuBtn.visibility = View.GONE
                itemViewBinding.bookmarkBtn.visibility = View.VISIBLE
            }
            Constants.MERGE -> {
                itemViewBinding.apply {
                    menuBtn.visibility = View.GONE
                    mergeCountTxt.visibility = View.VISIBLE
                    cardView.setBackgroundColor(
                        ResourcesCompat.getColor(
                            itemViewBinding.root.resources,
                            R.color.backgroundColor,
                            null
                        )
                    )
                    position = (adapterPosition + 1).toString()
                }
            }
        }
        itemViewBinding.apply {
            this.docModel = dataModel
        }
        itemViewBinding.root.setOnClickListener {
            Companions.selectedDocModelPosition = adapterPosition
            onItemClickListener?.let {
                it(dataModel)
            }
        }
        itemViewBinding.bookmarkBtn.setOnClickListener {
            onBookmarkClickListener?.let {
                it(dataModel.path, dataModel.isBookmarked,dataModel)
            }
        }
        itemViewBinding.menuBtn.setOnClickListener {view->
            onMenuClickListener?.let {
                it(dataModel)
            }
        }
    }

    private var onItemClickListener: ((DataModel) -> Unit)? = null
    private var onBookmarkClickListener: ((String, Boolean,DataModel) -> Unit)? = null
    private var onMenuClickListener: ((DataModel) -> Unit)? = null

    inner class DataViewHolder(val itemBinding: PdfListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindImageData(imageEntity: DataModel, holder: DataViewHolder, position: Int) {
            bindView(imageEntity, itemBinding, position)
        }
    }

    init {
        selectedItems = SparseBooleanArray()
    }

    private fun setData(
        isNativeAdEnable: Boolean,
        packsList: MutableList<DataModel>,
        isNetworkAvilable: Boolean,
        periority: Int,
        countAfter: Int, onComplete:(()->Unit)?=null
    ) {
//        GlobalScope.launch(Dispatchers.IO) {
            currentEntities.clear()
            itemList = packsList
            Log.d("favroite_list", "Pack List Size ${packsList.size}")
            packsList.forEachIndexed { index, imageEntity ->
                if (isNativeAdEnable && isNetworkAvilable && ((index != 0 && index % countAfter == 0) || index == Constants.FIRST_ADS_COUNT_AFTER)) {
                    currentEntities.add(NativeItemModel(null, periority))
                }
                Log.d("favroite_list", "Pack List Size ${currentEntities.size} item Added ${imageEntity.name}")
                currentEntities.add(DataItemModel(imageEntity))
            }
            Log.d("favroite_list", "ImagesEntity List Size ${currentEntities.size}")
//        }.invokeOnCompletion {
            GlobalScope.launch(Dispatchers.Main) {
                onComplete?.invoke()
            }
//        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = currentEntities[position]) {
            is NativeItemModel -> {
                if (adsHashMap[position] != null) {
                    item.nativeAd = adsHashMap[position] as Any
                }
                item.bindItem(holder, position)
            }
            is DataItemModel -> {
                item.bindItem(holder, position)
            }
        }
    }

    private class NativeItemModel(var nativeAd: Any?, var periority: Int) : BaseItemModel() {
        override fun itemType(): Int {
            return ItemTypeEnum.NATIVE_AD.ordinal
        }

        override fun bindItem(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as NativeAdsViewHolderClass).setData(nativeAd, position, periority)
        }

        var pos: Int = 0
        public fun setNewPos(mPos: Int) {
            this.pos = mPos
        }
    }

    class DataItemModel(var imageEntity: DataModel) : BaseItemModel() {
        override fun itemType(): Int {
            return ItemTypeEnum.REAL_ITEM.ordinal
        }

        override fun bindItem(holder: RecyclerView.ViewHolder?, position: Int) {
            holder as DataViewHolder
            holder.bindImageData(imageEntity, holder, position)
        }
    }

    fun updateLayout(
        newList: MutableList<DataModel>,
        isGridEnable: Boolean,
        isPremiumUser: Boolean,
        isInternetConnected: Boolean,
        sortBy: String, onComplete:(()->Unit)?=null
    ) {
        GlobalScope.launch((Dispatchers.Default)) {
            try {
                when (sortBy) {
                    Constants.SIZE -> {
                        newList.sortByDescending { it.encryptedLength }
                    }
                    Constants.NAME -> {
                        newList.sortBy { it.name.toLowerCase(Locale.ROOT) }
                    }
                    else -> {
                        newList.sortByDescending { it.encryptedTime }
                    }
                }
                GlobalScope.launch((Dispatchers.Main)) {
                    itemList.clear()
                    itemList.addAll(newList)
                    if (isPremiumUser) {
                        setData(
                            false,
                            itemList.toMutableList(),
                            isInternetConnected,
                            2,
                            Constants.ADS_COUNT_AFTER
                        ) {
                            notifyDataSetChanged()
                            onComplete?.invoke()
                        }
                    }
                    else {
                        if (type != Constants.SEARCH) {
                            setData(
                                true,
                                itemList.toMutableList(),
                                isInternetConnected,
                                2,
                                Constants.ADS_COUNT_AFTER
                            ) {
                                notifyDataSetChanged()
                                onComplete?.invoke()
                            }
                        }
                        else {
                            setData(
                                true,
                                itemList.toMutableList(),
                                isInternetConnected,
                                2,
                                Constants.ADS_COUNT_AFTER
                            ) {
                                notifyDataSetChanged()
                                onComplete?.invoke()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                GlobalScope.launch((Dispatchers.Main)) {
                    itemList.clear()
                    itemList.addAll(newList)
                    if (isPremiumUser) {
                        setData(
                            false,
                            itemList.toMutableList(),
                            isInternetConnected,
                            2,
                            Constants.ADS_COUNT_AFTER
                        ) {
                            notifyDataSetChanged()
                            onComplete?.invoke()
                        }
                    } else {
                        if (type != Constants.SEARCH) {
                            setData(
                                true,
                                itemList.toMutableList(),
                                isInternetConnected,
                                2,
                                Constants.ADS_COUNT_AFTER
                            ) {
                                notifyDataSetChanged()
                                onComplete?.invoke()
                            }
                        }
                        else {
                            setData(
                                true,
                                itemList.toMutableList(),
                                isInternetConnected,
                                2,
                                Constants.ADS_COUNT_AFTER
                            ) {
                                notifyDataSetChanged()
                                onComplete?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}