package com.reader.office.mychanges.interfaces

import java.io.File

interface OnRenameCallback {
    fun onRename(currentFile:File,newFile:File)
}