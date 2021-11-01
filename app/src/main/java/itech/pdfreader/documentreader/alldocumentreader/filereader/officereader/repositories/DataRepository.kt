package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.repositories

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.db.DocumentDao
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.db.DocumentsDatabase
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel

@Keep
class DataRepository(application: Application){

//    val isGrid = tinyDB.getBoolean(Constants.GRID_VALUE)
//
//    fun setGrid(isGrid: Boolean) {
//        tinyDB.putBoolean(Constants.GRID_VALUE, isGrid)
//    }


    private val documentDao : DocumentDao = DocumentsDatabase.getInstance(application).docModelDao()
    fun insertDocModel(dataModel: DataModel) : Long{
        return documentDao.insert(dataModel)
    }
    suspend fun updateDocModelName(tid: Long, fileName: String) : Int{
        return documentDao.updateFileName(tid, fileName)
    }
    suspend fun updateDocModel(dataModel: DataModel){
        return documentDao.updateDocModel(dataModel)
    }
//    suspend fun updateDocModel(docModel: DocModel)=docModelDao.updateFile(docModel)

    suspend fun findDocModel(id: Int):LiveData<DataModel>{
        return documentDao.find(id)
    }
    suspend fun getAllDocModel(): LiveData<List<DataModel>> {
        return documentDao.getAll()
    }

    fun getAllDocModelForCheck(): List<DataModel> {
        return documentDao.getAllForCheck()
    }


    suspend fun getMyDocModel(): LiveData<List<DataModel>> {
        return documentDao.getMyFiles()
    }


    suspend fun getRecentDocModels(): LiveData<List<DataModel>> {
        return documentDao.getAllRecentFiles()
    }
    suspend fun getBookmarkDocModels(): LiveData<List<DataModel>> {
        return documentDao.getAllBookmarkedFile()
    }

    suspend fun deleteDocModel(id: Long){
        documentDao.delete(id)
    }

    suspend fun deleteDocModelByPath(path: String){
        documentDao.delete(path)
    }

    suspend fun addBookmarkByPath(path:String, is_bookmark: Boolean?)=documentDao.updateBookmarkByPath(path, is_bookmark)

    suspend fun updateDocModelNameByLength(tid: Long, fileName: String) : Int{
        return documentDao.updateFileNameByLength(tid, fileName)
    }

    suspend fun updateDocModelPathBtLength(tid: Long, path: String) : Int{
        return documentDao.updateFilePathByLength(tid, path)
    }
    suspend fun updateDocModelDateBtLength(tid: Long, date: String) : Int{
        return documentDao.updateFileDateByLength(tid, date)
    }

//    suspend fun findDocModel(name: String) : LiveData<List<DataModel>>{
//        return documentDao.findBy(name)
//    }
    fun findDocModel(name: String) : List<DataModel>{
        return documentDao.findBy(name)
    }
    suspend fun addBookmark(path: String, is_bookmark: Boolean?)=documentDao.updateBookmark(path, is_bookmark)
    suspend fun addRecent(path: String, isRecent: Boolean?)=documentDao.updateRecent(path, isRecent)
    suspend fun addMyFile(tid: Long, isMyFile: Boolean?)=documentDao.updateMyFile(tid, isMyFile)
    suspend fun setIsLockFile(tid: Long, isLock: Boolean?)=documentDao.updateIsLock(tid, isLock)

    companion object {
        private const val LOG_TAG = "DocRepository"

        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(application: Application): DataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: DataRepository(application)
                        .also { INSTANCE = it }
            }

        private const val TAG = "DocRepository"
    }
}


