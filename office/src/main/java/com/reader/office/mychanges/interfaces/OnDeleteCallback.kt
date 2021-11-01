package com.reader.office.mychanges.interfaces

import java.io.File

interface OnDeleteCallback {
    fun onDelete(currentFile:File)
}