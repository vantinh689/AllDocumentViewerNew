package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "DocModel", indices = [Index(value = ["path"], unique = true)])
data class DataModel(
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "size") var size: String = "",
    @ColumnInfo(name = "type") var type: String = "",
    @ColumnInfo(name = "path") var path: String = "",
    @ColumnInfo(name = "lastModifiedTime") var lastModifiedTime: String = "",
    @ColumnInfo(name = "encryptedLength") var encryptedLength: Long = 0,
    @ColumnInfo(name = "encryptedTime") var encryptedTime: Long = 0,
    @ColumnInfo(name = "isRecent") var isRecent: Boolean = false,
    @ColumnInfo(name = "isBookmarked") var isBookmarked: Boolean = false,
    @ColumnInfo(name = "isMyFile") var isMyFile: Boolean = false,
    @ColumnInfo(name = "isLock") var isLock: Boolean = false,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
):Serializable{
    var itemType: String = ""
    var pos: Int = 0
    var isSelected:Boolean = false
}