package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.db

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.*
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel

@Keep
@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dataModel: DataModel): Long

    @Query("Delete from DocModel")
    fun deleteAll()

    @Query("Delete from DocModel Where id = :id")
    fun delete(id: Long)

    @Query("Delete from DocModel Where path = :path")
    fun delete(path: String)

    @Query("UPDATE DocModel SET isBookmarked = :is_bookmark WHERE path = :path")
    fun updateBookmarkByPath(path: String, is_bookmark: Boolean?): Int

    @Query("Select * from DocModel where id = :id")
    fun find(id: Int): LiveData<DataModel>

    @Query("Select * from DocModel Order By id Desc")
    fun getAll(): LiveData<List<DataModel>>

    @Query("Select * from DocModel Order By id Desc")
    fun getAllForCheck(): List<DataModel>

    @Query("Select * from  DocModel where isMyFile=1 Order By id Desc")
    fun getMyFiles(): LiveData<List<DataModel>>

    @Query("Select * from  DocModel where isRecent=1 Order By id Desc")
    fun getAllRecentFiles(): LiveData<List<DataModel>>
//
//    @Query("SELECT * FROM DocModel WHERE name LIKE '%' || :name || '%'")
//    fun findBy(name: String): LiveData<List<DataModel>>

    @Query("SELECT * FROM DocModel WHERE path LIKE '%' || :path || '%'")
    fun findBy(path: String): List<DataModel>


    @Query("SELECT * FROM DocModel where isBookmarked=1 ORDER BY id DESC ")
    fun getAllBookmarkedFile(): LiveData<List<DataModel>>

//    @Query("UPDATE DocModel SET isBookmarked = :is_bookmark WHERE id = :tid")
//    fun updateBookmark(tid: Int, is_bookmark: Boolean?): Int

//    @Query("UPDATE DocModel SET isRecent = :isRecent WHERE id = :tid")
//    fun updateRecent(tid: Int, isRecent: Boolean?): Int

    @Query("UPDATE DocModel SET isBookmarked = :is_bookmark WHERE path = :path")
    fun updateBookmark(path:String, is_bookmark: Boolean?): Int

    @Query("UPDATE DocModel SET isRecent = :isRecent WHERE path = :path")
    fun updateRecent(path: String, isRecent: Boolean?): Int

    @Query("UPDATE DocModel SET isMyFile = :isMyFile WHERE id = :tid")
    fun updateMyFile(tid: Long, isMyFile: Boolean?): Int

    @Query("UPDATE DocModel SET isLock = :isLock WHERE id = :tid")
    fun updateIsLock(tid: Long, isLock: Boolean?): Int

    @Query("UPDATE DocModel SET name = :name WHERE encryptedLength = :tid")
    fun updateFileNameByLength(tid: Long, name: String): Int

    @Query("UPDATE DocModel SET path = :path WHERE encryptedLength = :tid")
    fun updateFilePathByLength(tid: Long, path: String): Int

    @Query("UPDATE DocModel SET lastModifiedTime = :date WHERE encryptedLength = :tid")
    fun updateFileDateByLength(tid: Long, date: String): Int

    @Query("UPDATE DocModel SET name = :name WHERE id = :tid")
    fun updateFileName(tid: Long, name: String): Int

    @Update
    fun updateDocModel(dataModel: DataModel)
}