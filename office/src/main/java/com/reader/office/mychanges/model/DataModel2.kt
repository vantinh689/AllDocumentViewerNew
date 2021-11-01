package com.reader.office.mychanges.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import java.io.Serializable

@Keep
@Entity(tableName = "DocModel", indices = [Index(value = ["path"], unique = true)])
data class DataModel2(
    var name: String = "",
    var size: String = "",
    var type: String = "",
    var path: String = "",
    var lastModifiedTime: String = "",
    var encryptedLength: Long = 0,
    var encryptedTime: Long = 0,
    var isRecent: Boolean = false,
    var isBookmarked: Boolean = false,
    var isMyFile: Boolean = false,
    var isLock: Boolean = false,
    var id: Int = 0
):Serializable{
    var itemType: String = ""
    var pos: Int = 0
    var isSelected:Boolean = false
}