package com.reader.office.mychanges.interfaces

import java.io.File

interface OnBookmarkCallback {
    fun onBookmark(currentFile:File,isBookmark:Boolean)
}