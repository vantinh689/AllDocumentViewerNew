package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities

import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel

class Companions {
    companion object{
        var isAllFilesGridChange:Boolean = false
        var isRecentGridChange:Boolean = false
        var isMyFilesGridChange:Boolean = false
        var isAllFilesLoaded:Boolean = false

        var isRecentAdded = false

        var isChangeFromOtherModuleDone = false

        var newPath:String = ""
        var sortType:String = ""

        var isCallHomeFragment:Boolean=false
        var isPurchased:Boolean=false

        var homeScreenCounter=0
        var viewerScreenCounter=0
        var bottomSheetCounter=0
        var pdfListCounter=0
        var globalInterAdCounter=1

        var selectedDataModel:DataModel? = null
        var selectedDocModelPosition:Int = 0
        var isMergeSelected:Boolean = false

        var preLoadedNativeAd: Any? = null
    }
}