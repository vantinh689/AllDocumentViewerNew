package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

object Constants {

    const val textExtension = ".txt"
    const val RENAME: String = "RENAME"
    const val BITMAP_STICKER: String = "bitmapSticker"
    const val CANCEL: String = "Cancel"
    const val ALLOW: String = "allow"
    const val isRatingDoneFirstTime: String = "isRatingDoneFirstTime"
    const val SIZE = "size"
    const val NAME = "name"
    const val DATE = "date"
    const val appOpenCounterKey = "appOpenCounterKey"

    const val isPremiumUserKey = "isPremiumUserKey"

    const val NATIVE_AD = "nativeAD"
    const val IS_FROM_VIEWER: String = "isFromViewer"

    var isGetStartedBtnClick = "isGetStartedBtnClick"
    const val ADS_COUNT_AFTER = 8
    const val FIRST_ADS_COUNT_AFTER = 1

    const val STORAGE_LOCATION = "storage_location"

    const val excelExtension = ".xls"
    const val excelWorkbookExtension = ".xlsx"
    const val docExtension = ".doc"
    const val docxExtension = ".docx"
    const val PPT = ".ppt"
    const val PPTX = ".pptx"
    const val PDF: String = ".pdf"

    const val REQUEST_CODE_STT = 1

    const val MESSAGE: String = "Message"
    const val ALERT: String = "Alert"
    const val IS_FROM_HOME_SCREEN: String = "isFromHome"
    const val BOOKMARK: String = "bookmark"
    const val IS_VIEWER_NIGHT_MODE: String = "IS_VIEWER_NIGHT_MODE"
    const val IS_VIEWER_HORIZONTAL_VIEW: String = "IS_VIEWER_HORIZONTAL_VIEW"
    const val SEARCH = "Search"
    const val GRID_VALUE: String = "GridValue"
    const val AllFiles: String = "All Files"

    const val SELECT_FILE_REQUEST_CODE: Int = 13
    const val HEADER: String = "header"
    const val HOME_TOOLS: String = "HomeTools"
    const val TOOLS: String = "Tools"
    const val MERGE: String = "merge"
    const val MERGE_SELECTED: String = "mergeSelect"
    private const val MAIN_FOLDER = "/Cam Scanner/"
    const val TYPE = "FileType"

    const val DOC_MODEL = "file"
    const val PAGE_NO = "pageNo"
    const val NO_OF_PAGES = "noOFPages"

    const val EOF = -1

    //billing constants
    const val KEY_IS_PURCHASED = "KEY_IS_PURCHASED"

    const val BILLING_KEY: String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyj4dj5Y7vftuwOb1oDiHhmqAwE9DUvqwlkTRfeFI6zT/UiVvNoUW+QdmVpmyI1snn31ZCB16L8cPEZeLrTBYNr6ql/86McfWc3ao8ToOD1pWYAkhj71GwFD8JJDm6jTrtKZKEZ75ipRU16dyW4FBIc0QxbjKcEQjIaZ8t1Y+6aNlDKpLfN3J5gLUYf6GU4ZyU3PW+d/a9LNpbDkPHaf9A96zECbpbHJ7ZmhTLZoWoRu6ov+LhDOu0QqQ5TYz4jeLZdBUpQ+H6e3W2jYIl+RbGeluCy3Oyatxn6DBgmyYAMpgnioK7QIBE+ygRYGiP3cgKeulfGew5s4N8rmuCzzDxQIDAQAB"
    const val SUBSCRIBE_MONTHLY_PACKAGE: String = "monthly_package"
    const val SUBSCRIBE_ANNUAL_PACKAGE: String = "annual_subscription_package"

    val INAPP_SKUS = listOf("PURCHASE_LIFE_TIME")

    val SUBS_SKUS = listOf(
        SUBSCRIBE_MONTHLY_PACKAGE,
        SUBSCRIBE_ANNUAL_PACKAGE
    )

    val NON_CONSUMABLE_SKUS = SUBS_SKUS
}