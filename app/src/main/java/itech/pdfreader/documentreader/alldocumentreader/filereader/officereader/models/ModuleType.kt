package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models

import androidx.annotation.Keep

@Keep
enum class ModuleType {
    Compress,
    Merge,
    Edit,
    Reorder,
    AddRemovePassword,
    ExtractText,
    DocToPdf,
    ExcelToPdf
}