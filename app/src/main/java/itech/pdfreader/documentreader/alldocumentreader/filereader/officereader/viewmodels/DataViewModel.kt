package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.viewmodels

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories.DataRepository
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories.FilesRepository
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@Keep
class DataViewModel(var app: Application) : AndroidViewModel(app) {

    private var filesRepositories = FilesRepository.getInstance(app)
    private var docModelRepository = DataRepository.getInstance(app)

    var allFilesFromDevice = mutableListOf<DataModel>()

    val isHomeScreenObserver= MutableLiveData(false)

    val allDeviceFiles: ArrayList<DataModel> get() = filesRepositories.allFilesFromDevice
    val docFiles: ArrayList<DataModel> get() = filesRepositories.allDocFilesFromDevice
    val pdfFiles: ArrayList<DataModel> get() = filesRepositories.allPdfFilesFromDevice
    val excelFiles: ArrayList<DataModel> get() = filesRepositories.allExcelFilesFromDevice
    val pptFiles: ArrayList<DataModel> get() = filesRepositories.allPptFilesFromDevice

    val allFiles: LiveData<List<DataModel>> get() = allFilesList
    val searchFiles: LiveData<List<DataModel>> get() = searchFilesList
    val myFiles: LiveData<List<DataModel>> get() = myFilesList
    val dataModelRecentList: LiveData<List<DataModel>> get() = recentList
    val dataModelBookmarkList: LiveData<List<DataModel>> get() = bookmarkList

    private val recentList = MediatorLiveData<List<DataModel>>()
    private val bookmarkList = MediatorLiveData<List<DataModel>>()
    private val allFilesList = MediatorLiveData<List<DataModel>>()
    private val searchFilesList = MediatorLiveData<List<DataModel>>()
    private val myFilesList = MediatorLiveData<List<DataModel>>()
    val isObserverChangeAll = MutableLiveData(false)

    fun setObserveChanges(value: Boolean) {
        isObserverChangeAll.postValue(value)
    }

    fun updateData() {
        allFilesList.postValue(allFilesList.value)
        recentList.postValue(recentList.value)
        myFilesList.postValue(myFilesList.value)
    }

    fun updateDataAllFiles() {
        allFilesList.postValue(allFilesList.value)
    }

    suspend fun deleteDocModelByPath(path: String) {
        docModelRepository.deleteDocModelByPath(path)
    }

    suspend fun addBookmarkByPath(path: String, is_bookmark: Boolean?) {
        docModelRepository.addBookmarkByPath(path, is_bookmark)
    }

    fun removeFile(docModel: DataModel){
        allDeviceFiles.remove(docModel)
        docFiles      .remove(docModel)
        pdfFiles      .remove(docModel)
        excelFiles    .remove(docModel)
        pptFiles      .remove(docModel)
    }

    fun setObserveForRefreshHomeState(value: Boolean) {
        //filesRepositories.initializeAllFilesList()
        isHomeScreenObserver.postValue(value)
    }

    suspend fun getSearchResults(keyword: String) {
        searchFilesList.addSource(docModelRepository.getAllDocModel()) { doc ->
            searchFilesList.postValue(doc.filter {
                it.name.contains(keyword, true)
            })
        }
    }

    suspend fun addDataModel(docModel: DataModel): Long {
        return docModelRepository.insertDocModel(docModel)
    }

    fun findDataModel(path: String): List<DataModel> {
        return docModelRepository.findDocModel(path)
    }

    fun renameFile(currentFile: File,newFile:File,newDate:String){
        allDeviceFiles.find { it.path== currentFile.path }?.name = newFile.name
        docFiles      .find { it.path== currentFile.path }?.name = newFile.name
        pdfFiles      .find { it.path== currentFile.path }?.name = newFile.name
        excelFiles    .find { it.path== currentFile.path }?.name = newFile.name
        pptFiles      .find { it.path== currentFile.path }?.name = newFile.name

        allDeviceFiles.find { it.path== currentFile.path }?.path = newFile.path
        docFiles      .find { it.path== currentFile.path }?.path = newFile.path
        pdfFiles      .find { it.path== currentFile.path }?.path = newFile.path
        excelFiles    .find { it.path== currentFile.path }?.path = newFile.path
        pptFiles      .find { it.path== currentFile.path }?.path = newFile.path

        allDeviceFiles.find { it.path== currentFile.path }?.lastModifiedTime = newDate
        docFiles      .find { it.path== currentFile.path }?.lastModifiedTime = newDate
        pdfFiles      .find { it.path== currentFile.path }?.lastModifiedTime = newDate
        excelFiles    .find { it.path== currentFile.path }?.lastModifiedTime = newDate
        pptFiles      .find { it.path== currentFile.path }?.lastModifiedTime = newDate
    }

    suspend fun updateDocModelNameByLength(id: Long, fileName: String): Int {
        return docModelRepository.updateDocModelNameByLength(id, fileName)
    }
    suspend fun updateDocModelPathByLength(id: Long, path: String): Int {
        return docModelRepository.updateDocModelPathBtLength(id, path)
    }
    suspend fun updateDocModelDataByLength(id: Long, date: String): Int {
        return docModelRepository.updateDocModelDateBtLength(id, date)
    }

    suspend fun getAllSearchResults(keyword: String) {
        allFilesList.addSource(docModelRepository.getAllDocModel()) { doc ->
            allFilesList.postValue(doc.filter {
                it.name.contains(keyword, true)
            })
        }
        recentList.addSource(docModelRepository.getRecentDocModels()) { doc ->
            recentList.postValue(doc.filter {
                it.name.contains(keyword, true)
            })
        }
        myFilesList.addSource(docModelRepository.getMyDocModel()) { doc ->
            myFilesList.postValue(doc.filter {
                it.name.contains(keyword, true)
            })
        }
    }

    suspend fun getRecentDocList() {
        recentList.addSource(docModelRepository.getRecentDocModels()) { doc ->
            recentList.postValue(doc)
        }
    }

    suspend fun getBookmarkDocList() {
        bookmarkList.addSource(docModelRepository.getBookmarkDocModels()) { doc ->
            bookmarkList.postValue(
                doc
            )
        }
    }

    suspend fun getAllFiles() {
        allFilesList.addSource(docModelRepository.getAllDocModel()) { doc ->
            allFilesList.postValue(
                doc
            )
        }
    }
    fun getAllFilesForCheck() :List<DataModel>{
        return docModelRepository.getAllDocModelForCheck()
    }


    fun retrieveFilesFormDevice(onComplete: ((MutableList<DataModel>) -> Unit)? = null) {
        if (filesRepositories.allFilesFromDevice.isEmpty())
            GlobalScope.launch(Dispatchers.IO) {
                filesRepositories.getFilePaths()
            }.invokeOnCompletion {
                GlobalScope.launch(Dispatchers.Main) {
                    Timber.d(filesRepositories.allFilesFromDevice.size.toString())
                    Timber.d(filesRepositories.allDocFilesFromDevice.size.toString())
                    Timber.d(filesRepositories.allExcelFilesFromDevice.size.toString())
                    Timber.d(filesRepositories.allPdfFilesFromDevice.size.toString())
                    onComplete?.invoke((filesRepositories.allFilesFromDevice))
                }
            }
        else {
            onComplete?.invoke((filesRepositories.allFilesFromDevice))
        }
    }

    fun retrieveRefreshedFilesFormDevice(onComplete: ((MutableList<DataModel>) -> Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            filesRepositories.allFilesFromDevice.clear()
            filesRepositories.getFilePaths()
        }.invokeOnCompletion {
            GlobalScope.launch(Dispatchers.Main) {
                Timber.d(filesRepositories.allFilesFromDevice.size.toString())
                Timber.d(filesRepositories.allDocFilesFromDevice.size.toString())
                Timber.d(filesRepositories.allExcelFilesFromDevice.size.toString())
                Timber.d(filesRepositories.allPdfFilesFromDevice.size.toString())
                onComplete?.invoke((filesRepositories.allFilesFromDevice))
            }
        }
    }

    suspend fun getMyFiles() {
        myFilesList.addSource(docModelRepository.getMyDocModel()) { doc ->
            myFilesList.postValue(
                doc
            )
        }
    }

    suspend fun getAllFilesOnDevice(context: Context) {
        val storagePath = context.getStoragePathPdf()
        if (allFilesFromDevice.isNullOrEmpty()) {
            allFilesFromDevice.addAll(context.readFilesFromDir(File(storagePath)))
            val list = context.getAllPdfFiles()
            allFilesFromDevice.addAll(list)
        } else {
            allFilesFromDevice.addAll(context.readFilesFromDir(File(storagePath)))
        }
    }

    suspend fun updateDocModelName(tid: Long, fileName: String): Int {
        return docModelRepository.updateDocModelName(tid = tid, fileName)
    }

    suspend fun updateDocModel(dataModel: DataModel) {
        return docModelRepository.updateDocModel(dataModel)
    }

    suspend fun deleteDocModel(tid: Long) {
        docModelRepository.deleteDocModel(tid)
    }

    suspend fun addBookmark(path: String, is_bookmark: Boolean?) {
        docModelRepository.addBookmark(path, is_bookmark)
    }

    suspend fun addIsLock(tid: Long, isLock: Boolean?) {
        docModelRepository.setIsLockFile(tid, isLock)
    }

    suspend fun addRecent(path: String, isRecent: Boolean?) {
        docModelRepository.addRecent(path, isRecent)
    }

    suspend fun addMyFile(tid: Long, isMyFile: Boolean?) {
        docModelRepository.addMyFile(tid, isMyFile)
    }

    /*for merging module*/
    val mergeFilesBottomSheetList: LiveData<List<DataModel>> get() = mergeBottomSheetList
    private val mergeBottomSheetList = MediatorLiveData<List<DataModel>>()
    var mergeBottomSelectedFile: ((DataModel) -> Unit)? = null
    fun updateMergeFilesBottomSheetList(mergeFilesBottomSheetList: MutableList<DataModel>) {
        mergeBottomSheetList.postValue(mergeFilesBottomSheetList)
    }
}
